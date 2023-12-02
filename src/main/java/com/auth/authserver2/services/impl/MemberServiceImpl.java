package com.auth.authserver2.services.impl;

import com.auth.authserver2.domains.map.MemberRoleEntity;
import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberUpdateDto;
import com.auth.authserver2.exceptions.ConfirmationTokenDoesNotExistException;
import com.auth.authserver2.exceptions.MemberDoesNotExistException;
import com.auth.authserver2.exceptions.UnexpectedConfirmationTokenUpdateException;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.repositories.ConfirmationTokenRepository;
import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.repositories.MemberRoleMapRepository;
import com.auth.authserver2.repositories.RolesRepository;
import com.auth.authserver2.services.EmailSenderService;
import com.auth.authserver2.services.MemberService;
import com.auth.authserver2.services.TokenService;
import com.auth.authserver2.utils.MemberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static com.auth.authserver2.domains.roles.Role.*;

@Slf4j
@Service("memberService")
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberRoleMapRepository memberRoleMapRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Qualifier("tokenService")
    private final TokenService tokenService;
    @Qualifier("emailSenderService")
    private final EmailSenderService emailSenderService;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository, MemberRoleMapRepository memberRoleMapRepository, RolesRepository rolesRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService, EmailSenderService emailSenderService) {
        this.memberRepository = memberRepository;
        this.memberRoleMapRepository = memberRoleMapRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.emailSenderService = emailSenderService;
    }

    //todo: change from string to a response that contains isSuccessful, msg and the token
    //todo: The architecture used from UnkownKoder is this https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
    public String loginUser(String username, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            return tokenService.generateJwt(auth);
        } catch (AuthenticationException exception) {
            log.info("Failed to authenticate: {}", exception.getMessage());
            return "Failed to authenticate: " + exception.getMessage();
        }
    }

    @Override
    public Optional<MemberDto> getMemberByEmail(String email) {
        Assert.hasText(email, "email cannot be empty");
        var member = memberRepository.findMemberEntityByEmail(email);
        return member.map(memberEntity -> Optional.ofNullable(MemberUtil.toDto(memberEntity))).orElse(null);
    }


    @Override
    @Transactional
    public ResponseMessage save(MemberDto newMember) {
        Assert.hasText(newMember.getUsername(), "username cannot be empty");
        Assert.hasText(newMember.getEmail(), "email cannot be empty");
        Assert.hasText(newMember.getPassword(), "password cannot be empty");
        Assert.hasText(newMember.getFirstname(), "firstname cannot be empty");
        Assert.hasText(newMember.getLastname(), "lastname cannot be empty");
        Assert.hasText(newMember.getRegisteredToClientId(), "registeredToClientId cannot be empty");

        if (memberRepository.findMemberEntityByUsername(newMember.getUsername().toLowerCase()).isPresent() || memberRepository.findMemberEntityByEmail(newMember.getEmail().toLowerCase()).isPresent()) {
            return new ResponseMessage(false, "Could not save the desired member. Member already exists");
        }

        //how do we actually save to a database that contains a many-to-many mapping table?
        //you first save the two separate entities, in our case roles and members
        //then you save their IDs to the mapping table to simulate the relationship.
        //This is probably over-engineered, a simple ManyToMany would suffice.
        newMember.setEnabled(false);
        newMember.setAccountNonExpired(true);
        newMember.setAccountNonLocked(true);
        newMember.setCredentialsNonExpired(true);
        newMember.setPassword(passwordEncoder.encode(newMember.getPassword()));
        newMember.setMemberRoles(Set.of(ROLE_USER, ROLE_GUEST, ROLE_NONE));

        //1. Member
        var memberEntity = MemberUtil.toNewEntity(newMember);
        var savedMember = memberRepository.save(memberEntity);

        //2. Roles
        //for each role that the used had, iterate over and save with the member (this creates the mapping in the map table).
        newMember.getMemberRoles().forEach(role -> {
            var roleEntity = rolesRepository.findRolesEntityByRoleName(role.getRole());
            roleEntity.ifPresent(rolesEntity -> memberRoleMapRepository.save(new MemberRoleEntity(rolesEntity, savedMember)));
        });

        //create token and send for email validation
        var confirmationToken = tokenService.saveConfirmationToken(tokenService.createConfirmationTokenEntity(savedMember));

        emailSenderService.sendEmailToNewUser(savedMember.getEmail(), confirmationToken.getToken());

        return new ResponseMessage(true, "Saved new member");
    }

    @Override
    @Transactional
    public ResponseMessage deleteMemberByEmail(String email) {

        if (memberRepository.findMemberEntityByEmail(email).isPresent()) {

            //When deleting, you must first delete the entity where the members foreign key(s) are!
            //then you can delete the actual original entity.
            Long memberId = memberRepository.findMemberEntityByEmail(email).get().getId();
            memberRoleMapRepository.deleteAllByMemberId(memberId); //<1> delete from mapping
            memberRepository.deleteAllById(memberId); //<2> delete actual entity

            if (memberRepository.findMemberEntityByEmail(email).isEmpty()) {
                return new ResponseMessage(true, "Member with email " + email + " has been deleted");
            } else {
                throw new RuntimeException("Could not delete member, check DB logs");
            }
        } else {
            return new ResponseMessage(false, "Member with email " + email + " could not be found");
        }
    }

    @Override
    @Transactional
    public ResponseMessage updateMemberCredentials(MemberUpdateDto member) {

        member.setId(extractMemberId());

        var existingMember = memberRepository.findMemberEntityById(Long.valueOf(member.getId()));

        //todo: fix the validateMember in memberUtil also, make it throw custom exceptions (not the predefined ones in use atm).
        if (existingMember.isPresent()) {
            var validation = MemberUtil.validateMemberDto(member);
            if (validation.isSuccessful()) {
                var updatedMember = MemberUtil.toExistingEntityWithUpdatedCredentials(member, existingMember.get());
                memberRepository.save(updatedMember);
                String msg = String.format("Updated member %s to %s", existingMember.get(), updatedMember);
                return new ResponseMessage(true, msg);
            } else {
                return new ResponseMessage(false, validation.getMsg());
            }
        } else {
            return new ResponseMessage(false, "Not a valid username");
        }
    }

    @Override
    public ResponseMessage confirmMember(String token) {
        var confirmationToken = tokenService.getToken(token);
        if (confirmationToken.isPresent()) {
            if (confirmationToken.get().getExpiresAt().isBefore(Instant.now())) { //if token is expired (guard-block)
                var foundMember = memberRepository.findMemberEntityById(
                        tokenService.findMemberEntityByToken(token).getId());
                if (foundMember.isPresent()) { //if member exists
                    var newConfirmationToken = tokenService.saveConfirmationToken(tokenService.createConfirmationTokenEntity(foundMember.get()));
                        emailSenderService.sendEmailToNewUser(memberRepository.findMemberEntityById(newConfirmationToken.getMemberEntity().getId()).get().getEmail(), newConfirmationToken.getToken());
                        return new ResponseMessage(false, "Old confirmation token had expired. Sent out a new one to the specified email " + newConfirmationToken.getMemberEntity().getEmail());
                } else {
                    throw new MemberDoesNotExistException("Member does not exist"); //this should only happen if someone randomly sends in a shitty string
                }
            }
            var updatedToken = tokenService.updateMemberConfirmationTokenWhenConfirmingAccount(token); //if token is still valid
            if (updatedToken.getConfirmedAt().minusSeconds(30).isBefore(Instant.now())) { //Only way to check if the token has actually been updated at runtime, in DB
                var updatedRows = memberRepository.updateMemberEnabledById(updatedToken.getMemberEntity().getId());
                log.info("Accessing memberRepository.updateMemberEnabledById(), #{} updated rows", updatedRows);
                return new ResponseMessage(true, "Account confirmed");
            } else {
                throw new UnexpectedConfirmationTokenUpdateException("Error in updating token, update took more than 30 seconds to carry out.");
            }
        } else {
            throw new ConfirmationTokenDoesNotExistException("Could not find the specified token " + token);
        }
    }

    @Override
    public String extractMemberId() {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return auth.getToken().getClaimAsString("memberId");
    }


}


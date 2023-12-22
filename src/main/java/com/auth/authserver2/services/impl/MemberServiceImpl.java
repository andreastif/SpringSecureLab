package com.auth.authserver2.services.impl;

import com.auth.authserver2.domains.map.MemberRoleEntity;
import com.auth.authserver2.domains.member.MemberCheckSessionDto;
import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberLoginResponseDto;
import com.auth.authserver2.domains.member.MemberUpdateDto;
import com.auth.authserver2.exceptions.ConfirmationTokenDoesNotExistException;
import com.auth.authserver2.exceptions.MemberDoesNotExistException;
import com.auth.authserver2.exceptions.UnexpectedConfirmationTokenUpdateException;
import com.auth.authserver2.exceptions.UnexpectedMemberNotFoundException;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.repositories.MemberRoleMapRepository;
import com.auth.authserver2.repositories.RolesRepository;
import com.auth.authserver2.services.EmailSenderService;
import com.auth.authserver2.services.MemberService;
import com.auth.authserver2.services.TokenService;
import com.auth.authserver2.utils.MemberUtil;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import static com.auth.authserver2.domains.roles.Role.*;

@Slf4j
@Service("memberService")
public class MemberServiceImpl implements MemberService {

    private final JwtDecoder jwtDecoder;
    private final MemberRepository memberRepository;
    private final MemberRoleMapRepository memberRoleMapRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Qualifier("tokenService")
    private final TokenService tokenService;
    @Qualifier("emailSenderService")
    private final EmailSenderService emailSenderService;
    @Qualifier("userDetailsService")
    private final UserDetailsService userDetailsService;

    @Autowired
    public MemberServiceImpl(JwtDecoder jwtDecoder, MemberRepository memberRepository, MemberRoleMapRepository memberRoleMapRepository, RolesRepository rolesRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService, EmailSenderService emailSenderService, UserDetailsService userDetailsService) {
        this.jwtDecoder = jwtDecoder;
        this.memberRepository = memberRepository;
        this.memberRoleMapRepository = memberRoleMapRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.emailSenderService = emailSenderService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Cookie loginUser(String username, String password) {
        log.info("Calling loginUser with username: {} in memberService", username);

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            return tokenService.convertJwtToCookie(tokenService.generateJwt(auth));
        } catch (AuthenticationException exception) {
            log.info("Failed to authenticate: {}", exception.getMessage());
            throw new AuthenticationServiceException("Failed to authenticate");
        }
    }

    @Override
    public MemberDto getMemberByEmail(String email) {
        log.info("Calling getMemberByEmail with email: {} in memberService", email);
        Assert.hasText(email, "email cannot be empty");
        var member = memberRepository.findMemberEntityByEmail(email);
        if (member.isPresent()) {
            return MemberUtil.toDto(member.get());
        } else {
            throw new MemberDoesNotExistException("The specified member does not exist");
        }
    }


    @Override
    @Transactional
    public ResponseMessage save(MemberDto newMember) {
        log.info("Calling save in memberService");
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
        newMember.setMemberRoles(Set.of(ROLE_USER, ROLE_GUEST, ROLE_NONE, ROLE_ADMIN)); //todo: remove ROLE_ADMIN from here when admincontroller is up

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
        log.info("Calling deleteMemberByEmail for member with email: {} in memberService", email);
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
        log.info("Calling updateMemberCredentials for member with memberId: {} in memberService", member.getId());

        member.setId(extractMemberId());
        var existingMember = memberRepository.findMemberEntityById(Long.valueOf(member.getId()));
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
            return new ResponseMessage(false, "Could not find member");
        }
    }

    @Override
    public ResponseMessage confirmMember(String token) {
        log.info("Calling confirmMember with token: {} in memberService", token);

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
    public MemberCheckSessionDto checkSession() {
        log.info("Calling checkSession in memberService");
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var status = MemberCheckSessionDto.builder()
                .isAdmin(false)
                .isLoggedIn(false)
                .build();
        var btw = Duration.between(Instant.now(), auth.getToken().getExpiresAt());
        if (btw.isPositive()) {
            status.setIsLoggedIn(true);
            status.setExpiryTimeMillis(auth.getToken().getExpiresAt().toEpochMilli());
        }
        if (auth.getAuthorities().stream().anyMatch(e -> e.getAuthority().equals("ROLE_ADMIN"))) {
            status.setIsAdmin(true);
        }
        log.info("Status populated in checkSession: {}", status);
        return status;
    }

    @Override
    public MemberLoginResponseDto populateMemberLoginResponseDto(Cookie cookie) {
        log.info("Calling populateMemberLoginResponseDto with in memberService");
        Jwt jwt = jwtDecoder.decode(cookie.getValue());
        String roles = jwt.getClaim("roles");
        Long expiryInEpochMilliSeconds = jwt.getExpiresAt().toEpochMilli();
        log.info("Extracting values from cookie in memberService");
        var response = MemberLoginResponseDto
                .builder()
                .roles(roles)
                .expiryTimeMillis(expiryInEpochMilliSeconds)
                .build();
        log.info("Returning response object: {}", response);
        return response;
    }

    @Override
    @Transactional
    public Cookie logoutUser(Cookie[] cookies) {
        log.info("Calling logoutUser in memberService");
        Cookie extractedJwtCookie = tokenService.extractJwtCookie(cookies);
        tokenService.blacklistJwt(extractedJwtCookie);
        Cookie invalidatedCookie = tokenService.invalidateCookie();
        printCookie(invalidatedCookie);
        return invalidatedCookie;
    }

    @Override
    @Transactional
    public Cookie refreshSession(Cookie[] cookies) {
        log.info("Calling refreshSession in memberService");
        String memberId = extractMemberId();
        var member = memberRepository.findMemberEntityById(Long.valueOf(memberId));
        if (member.isPresent()) {

            // Extract the current JWT
            Cookie extractedJwtCookie = tokenService.extractJwtCookie(cookies);
            // Use UserDetailsService to load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(member.get().getUsername());
            // Create a new authentication token using UserDetails
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
            // Blacklist the old JWT
            tokenService.blacklistJwt(extractedJwtCookie);
            // Generate and return a new JWT using the generateJwt method
            return tokenService.convertJwtToCookie(tokenService.generateJwt(auth));
        } else {
            //Unless anything unexpected occurs, by design, if all systems are up and running, the member will always exist at this point
            throw new UnexpectedMemberNotFoundException("Member with memberId: " + memberId + " does not exist");
        }
    }


    @Override
    public String extractMemberId() {
        log.info("Calling extractMemberId in memberService");
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return auth.getToken().getClaimAsString("memberId");
    }


    private void printCookie(Cookie cookie) {
        log.info("===Printing cookie===");
        log.info("Cookie.getValue(): {}", cookie.getValue());
        log.info("Cookie.getName(): {}", cookie.getName());
        log.info("Cookie.getMaxAge(): {}", cookie.getMaxAge());
        log.info("Cookie.getAttributes(): {}", cookie.getAttributes());
        log.info("Cookie.getDomain(): {}", cookie.getDomain());
        log.info("Cookie.getPath(): {}", cookie.getPath());
        log.info("Cookie.getSecure(): {}", cookie.getSecure());
        log.info("===End of print===");
    }


}


package com.auth.authserver2.services.impl;

import com.auth.authserver2.domains.map.MemberRoleEntity;
import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberSecurity;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.repositories.MemberRoleMapRepository;
import com.auth.authserver2.repositories.RolesRepository;
import com.auth.authserver2.services.MemberService;
import com.auth.authserver2.utils.MemberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.Set;

import static com.auth.authserver2.domains.roles.Role.*;

@Service("memberService")
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberRoleMapRepository memberRoleMapRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository, MemberRoleMapRepository memberRoleMapRepository, RolesRepository rolesRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.memberRoleMapRepository = memberRoleMapRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.hasText(username, "username cannot be empty");

        return memberRepository
                .findMemberEntityByUsername(username)
                .map(MemberSecurity::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
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

        if (memberRepository.findMemberEntityByUsername(newMember.getUsername().toLowerCase()).isPresent() || memberRepository.findMemberEntityByEmail(newMember.getEmail().toLowerCase()).isPresent()) {
            return new ResponseMessage(false, "Could not save the desired member. Member already exists");
        }

        //how do we actually save to a database that contains a many-to-many mapping table?
        //you first save the two separate entities, in our case roles and members
        //then you save their IDs to the mapping table to simulate the relationship.
        //This is probably over-engineered, a simple ManyToMany would suffice.
        newMember.setEnabled(true);
        newMember.setAccountNonExpired(true);
        newMember.setAccountNonLocked(true);
        newMember.setCredentialsNonExpired(true);
        newMember.setPassword(passwordEncoder.encode(newMember.getPassword()));

        //todo: ska EJ få admin i denna service från memberController, Endast i adminController kan man ge Admin
        newMember.setMemberRoles(Set.of(ROLE_USER, ROLE_GUEST, ROLE_NONE));

        //1. Member
        var memberEntity = MemberUtil.toNewEntity(newMember);
        var member = memberRepository.save(memberEntity);

        //2. Roles
        //for each role that the used had, iterate over and save with the member (this creates the mapping in the map table).
        newMember.getMemberRoles().forEach(role -> {
            var roleEntity = rolesRepository.findRolesEntityByRoleName(role.getRole());
            roleEntity.ifPresent(rolesEntity -> memberRoleMapRepository.save(new MemberRoleEntity(rolesEntity, member)));
        });

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


    //todo: denna metod behöver få mer funktionalitet och få pathvariable m member id
    //todo: lägg in update för alla andra variabler
    @Override
    @Transactional
    public ResponseMessage updateMemberCredentials(MemberDto member) { //updaterar endast username atm

        var existingMember = memberRepository.findMemberEntityByEmail(member.getEmail().toLowerCase());

        if (existingMember.isPresent()) {
            String oldUsername = existingMember.get().getUsername();
            if (member.getUsername() != null && memberRepository.findMemberEntityByUsername(member.getUsername()).isEmpty()) {
                existingMember.get().setUsername(member.getUsername());
                memberRepository.save(existingMember.get());
                String msg = String.format("Updated username %s to %s", oldUsername, member.getUsername());
                return new ResponseMessage(true, msg);
            } else {
                return new ResponseMessage(false, "Not a valid username");
            }
        } else  {
            return new ResponseMessage(false, "API ERROR");
        }

    }
}


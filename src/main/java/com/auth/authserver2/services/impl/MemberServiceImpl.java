package com.auth.authserver2.services.impl;

import com.auth.authserver2.domains.map.MemberRoleEntity;
import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberEntity;
import com.auth.authserver2.domains.member.MemberSecurity;
import com.auth.authserver2.domains.roles.Role;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.repositories.MemberRoleMapRepository;
import com.auth.authserver2.repositories.RolesRepository;
import com.auth.authserver2.services.MemberService;
import com.auth.authserver2.utils.MemberUtil;
import com.auth.authserver2.utils.RoleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("memberService")
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberRoleMapRepository memberRoleMapRepository;
    private final RolesRepository rolesRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository, MemberRoleMapRepository memberRoleMapRepository, RolesRepository rolesRepository) {
        this.memberRepository = memberRepository;
        this.memberRoleMapRepository = memberRoleMapRepository;
        this.rolesRepository = rolesRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return memberRepository
                .findMemberEntityByUsername(username)
                .map(MemberSecurity::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public Optional<MemberDto> getMemberByEmail(String email) {
        var member = memberRepository.findMemberEntityByEmail(email);
        return member.map(memberEntity -> Optional.ofNullable(MemberUtil.toDto(memberEntity))).orElse(null);
    }

    @Override
    @Transactional
    public ResponseMessage save(MemberDto newMember) {


        if (memberRepository.findMemberEntityByUsername(newMember.getUsername().toLowerCase()).isPresent() || memberRepository.findMemberEntityByEmail(newMember.getEmail().toLowerCase()).isPresent()) {
            return new ResponseMessage(false, "Could not save the desired member. Member already exists");
        }

        //how do we actually save to a database that contains a many-to-many mapping table?
        //you first save the two separate entities, in our case roles and members
        //then you save their IDs to the mapping table to simulate the relationship.
        //This is probably over-engineered, a simple ManyToMany would suffice.


        //1. Member
        var memberEntity = MemberUtil.toEntity(newMember);
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
    public ResponseMessage deleteMemberByEmail(String email) {
        return new ResponseMessage(true, "replace me");
    }

    @Override
    public ResponseMessage updateMemberCredentials(MemberDto member) {
        return new ResponseMessage(true, "replace me");
    }


}

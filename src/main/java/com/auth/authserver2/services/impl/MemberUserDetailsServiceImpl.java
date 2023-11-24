package com.auth.authserver2.services.impl;

import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.repositories.MemberRoleMapRepository;
import com.auth.authserver2.repositories.RolesRepository;
import com.auth.authserver2.services.MemberUserDetailsService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("memberService")
public class MemberUserDetailsServiceImpl implements MemberUserDetailsService {


    @Autowired
    public MemberUserDetailsServiceImpl(MemberRepository memberRepository, MemberRoleMapRepository memberRoleMapRepository, RolesRepository rolesRepository) {
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return null;
    }
    @Override
    public MemberDto getMemberByEmail(String email) {
        return null;
    }

    @Override
    public ResponseMessage save(MemberDto newMember) {
        return new ResponseMessage(true, "replace me");
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

package com.auth.authserver2.services;

import com.auth.authserver2.domains.member.MemberDto;

import com.auth.authserver2.domains.member.MemberEntity;
import com.auth.authserver2.messages.ResponseMessage;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;


public interface MemberService extends UserDetailsService {
    Optional<MemberDto> getMemberByEmail(String email);

    ResponseMessage save(MemberDto newMember);

    ResponseMessage deleteMemberByEmail(String email);

    ResponseMessage updateMemberCredentials(MemberDto member);

}

package com.auth.authserver2.services;

import com.auth.authserver2.domains.member.MemberDto;

import com.auth.authserver2.domains.member.MemberEntity;
import com.auth.authserver2.domains.member.MemberUpdateDto;
import com.auth.authserver2.messages.ResponseMessage;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;


public interface MemberService {
    Optional<MemberDto> getMemberByEmail(String email);
    Optional<String> getMemberIdByUsername(String username);
    ResponseMessage save(MemberDto newMember);

    ResponseMessage deleteMemberByEmail(String email);

    ResponseMessage updateMemberCredentials(MemberUpdateDto member);

    String loginUser(String username, String password);

    String extractMemberId(MemberDto member);
    String extractMemberId(MemberUpdateDto member);

}
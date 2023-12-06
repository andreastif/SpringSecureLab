package com.auth.authserver2.services;

import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberUpdateDto;
import com.auth.authserver2.messages.ResponseMessage;
import jakarta.servlet.http.Cookie;

import java.util.Optional;


public interface MemberService {
    Optional<MemberDto> getMemberByEmail(String email);

    ResponseMessage save(MemberDto newMember);

    ResponseMessage deleteMemberByEmail(String email);

    ResponseMessage updateMemberCredentials(MemberUpdateDto member);

    Cookie loginUser(String username, String password);

    String extractMemberId();

    ResponseMessage confirmMember(String token);
}

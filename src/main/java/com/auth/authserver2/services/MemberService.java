package com.auth.authserver2.services;

import com.auth.authserver2.domains.member.MemberCheckSessionDto;
import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberLoginResponseDto;
import com.auth.authserver2.domains.member.MemberUpdateDto;
import com.auth.authserver2.messages.ResponseMessage;
import jakarta.servlet.http.Cookie;


public interface MemberService {
    MemberDto getMemberByEmail(String email);

    ResponseMessage save(MemberDto newMember);

    ResponseMessage deleteMemberByEmail(String email);

    ResponseMessage updateMemberCredentials(MemberUpdateDto member);

    Cookie loginUser(String username, String password);

    String extractMemberId();

    ResponseMessage confirmMember(String token);

    MemberCheckSessionDto checkSession();

    MemberLoginResponseDto populateMemberLoginResponseDto(Cookie cookie);
}

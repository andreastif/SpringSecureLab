package com.SpringSecureLab.services;

import com.SpringSecureLab.domains.member.MemberLoginResponseDto;
import com.SpringSecureLab.messages.ResponseMessage;
import com.SpringSecureLab.domains.member.MemberDto;
import com.SpringSecureLab.domains.member.MemberUpdateDto;
import jakarta.servlet.http.Cookie;


public interface MemberService {
    MemberDto getMemberByEmail(String email);

    ResponseMessage save(MemberDto newMember);

    ResponseMessage deleteMemberByEmail(String email);

    ResponseMessage updateMemberCredentials(MemberUpdateDto member);

    Cookie loginUser(String username, String password);

    String extractMemberId();

    ResponseMessage confirmMember(String token);

    Cookie checkSession();

    MemberLoginResponseDto populateMemberLoginResponseDto(Cookie cookie);

    Cookie logoutUser(Cookie[] cookies);

    Cookie refreshSession(Cookie[] cookies);
}

package com.SpringSecureLab.services;

import com.SpringSecureLab.domains.tokens.ConfirmationTokenEntity;
import com.SpringSecureLab.domains.tokens.JwtRepoEntity;
import com.SpringSecureLab.domains.member.MemberEntity;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface TokenService {

    String generateJwt(Authentication auth);

    ConfirmationTokenEntity saveConfirmationToken(ConfirmationTokenEntity confirmationToken);

    Optional<ConfirmationTokenEntity> getToken(String token);

    ConfirmationTokenEntity createConfirmationTokenEntity(MemberEntity memberEntity);

    ConfirmationTokenEntity updateMemberConfirmationTokenWhenConfirmingAccount(String token);

    MemberEntity findMemberEntityByToken(String token);

    Cookie convertJwtToCookie(String jwt);

    Cookie invalidateCookie();

    void blacklistJwt(Cookie cookie);

    Cookie extractJwtCookie(Cookie[] cookies);

    JwtRepoEntity isTokenBlacklisted(String jwt);

}

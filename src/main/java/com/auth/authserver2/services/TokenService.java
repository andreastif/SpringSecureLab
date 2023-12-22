package com.auth.authserver2.services;

import com.auth.authserver2.domains.member.MemberEntity;
import com.auth.authserver2.domains.tokens.ConfirmationTokenEntity;
import com.auth.authserver2.domains.tokens.JwtRepoEntity;
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

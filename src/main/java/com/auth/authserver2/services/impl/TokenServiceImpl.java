package com.auth.authserver2.services.impl;


import com.auth.authserver2.domains.member.MemberEntity;
import com.auth.authserver2.domains.tokens.ConfirmationTokenEntity;
import com.auth.authserver2.domains.tokens.JwtRepoEntity;
import com.auth.authserver2.exceptions.UnexpectedConfirmationTokenUpdateException;
import com.auth.authserver2.repositories.BlacklistTokenRepository;
import com.auth.authserver2.repositories.ConfirmationTokenRepository;
import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.services.TokenService;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service("tokenService")
public class TokenServiceImpl implements TokenService {

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;

    private final MemberRepository memberRepository;

    private final ConfirmationTokenRepository confirmationTokenRepository;

    private final BlacklistTokenRepository blacklistTokenRepository;

    @Autowired
    public TokenServiceImpl(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, MemberRepository memberRepository, ConfirmationTokenRepository confirmationTokenRepository, BlacklistTokenRepository blacklistTokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.memberRepository = memberRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.blacklistTokenRepository = blacklistTokenRepository;
    }

    public String generateJwt(Authentication auth) {
        log.info("Calling generateJwt in tokenService");
        Instant now = Instant.now();

        String scope = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String memberId = getMemberIdByUsername(auth.getName()).orElseThrow( () -> new RuntimeException("MemberId not found, check logs"));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString()) //jti or id
                .issuer("http://localhost:8080") //who issued
                .issuedAt(now) //when it was issued
//                .expiresAt(now.plusSeconds(1800)) //when it expires (30 min from issuing) //todo: activate me when testing is done!
                .expiresAt(now.plusSeconds(240)) //when it expires (30 min from issuing)
                .audience(List.of("http://localhost:8080")) //who it is intended for
                .subject(auth.getName()) //who it concerns
                .claim("roles", scope) //roles
                .claim("memberId", memberId)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }


    @Override
    @Transactional
    public ConfirmationTokenEntity saveConfirmationToken(ConfirmationTokenEntity confirmationToken) {
        log.info("Calling saveConfirmationToken in tokenService");
        return confirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public Optional<ConfirmationTokenEntity> getToken(String token) {
        log.info("Calling getToken in tokenService");
        return confirmationTokenRepository.findConfirmationTokenEntityByToken(token);
    }

    @Override
    public ConfirmationTokenEntity createConfirmationTokenEntity(MemberEntity memberEntity) {
        log.info("Calling createConfirmationTokenEntity in tokenService");
        return ConfirmationTokenEntity
                .builder()
                .token(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400)) //24 hours
                .memberEntity(memberEntity)
                .build();
    }

    @Override
    public ConfirmationTokenEntity updateMemberConfirmationTokenWhenConfirmingAccount(String token) {
        log.info("Calling updateMemberConfirmationTokenWhenConfirmingAccount with token: {} in tokenService", token );
        int update = confirmationTokenRepository.updateConfirmationTokenEntity(token, Instant.now());
        if (update > 0) {
            log.info("updateMemberConfirmationTokenWhenConfirmingAccount #{} of rows updated ", update);
            return confirmationTokenRepository.findConfirmationTokenEntityByToken(token).get();
        }
        log.debug("Update (no of rows updated) = {}", update);
        throw new UnexpectedConfirmationTokenUpdateException("Token could not be updated, check logs and database");
    }

    @Override
    public MemberEntity findMemberEntityByToken(String token) {
        log.info("Calling findMemberEntityByToken in tokenService");
        var foundToken = confirmationTokenRepository.findConfirmationTokenEntityByToken(token);
        return foundToken.map(ConfirmationTokenEntity::getMemberEntity).orElse(null);
    }

    @Override
    public Cookie convertJwtToCookie(String jwt) {
        log.info("Calling convertJwtToCookie in tokenService");
        Cookie jwtCookie = new Cookie("JWT_COOKIE", jwt);

        Instant expiry = Instant.parse(jwtDecoder.decode(jwt).getClaimAsString("exp"));
        Instant now = Instant.now();
        Duration betweenValue = Duration.between(now, expiry);


        jwtCookie.setHttpOnly(true); //putting the JWT inside a cookie and making it unreadable for javascript

//        jwtCookie.setSecure(true); // USE THIS WHEN SWITCHING OVER TO HTTPS, CRUCIAL

        //Sets the scope of the cookie, ie for what endpoints the cookie will be automatically sent to, by the browser.
        //For example, if you set the path of a cookie to /app, the cookie will be included in requests to /app and its sub-paths (like /app/user)
        // but not to other paths outside of /app.
        jwtCookie.setPath("/api/v1");
//        jwtCookie.setMaxAge((int) betweenValue.getSeconds()); //the browser will automatically decrement the variable in frontend //todo: activate me!
        jwtCookie.setMaxAge(240); //todo: replace with above after testing functionality is done

        //samesite attribute which protects against CSRF attacks. Sends cookie for GET but no other http method when using an email
        // or sending as link (post/put/patch etc only works when sending from origin where cookie is first originated).
        jwtCookie.setAttribute("SameSite", "Lax");
        return jwtCookie;
    }

    @Override
    public Cookie invalidateCookie() {
        log.info("Calling invalidateCookie in tokenService");
        Cookie invalidatedCookie = new Cookie("JWT_COOKIE", null);
        invalidatedCookie.setHttpOnly(true);
        invalidatedCookie.setPath("/api/v1");
        invalidatedCookie.setMaxAge(0);
        invalidatedCookie.setAttribute("SameSite", "Lax");
        return invalidatedCookie;
    }

    @Override
    public void blacklistJwt(Cookie cookie) {
        log.info("Calling blacklistJwt in tokenService");
        var expiry = jwtDecoder.decode(cookie.getValue()).getExpiresAt();
        var result = blacklistTokenRepository.save(new JwtRepoEntity(cookie.getValue(), expiry));
        log.info("Blacklisted token with values: {}", result);
    }

    @Override
    public Cookie extractJwtCookie(Cookie[] cookies) {
        log.info("Calling extractJwtCookie in tokenService");
        var foundCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("JWT_COOKIE")).toList();
        return foundCookie.getFirst();
    }

    @Override
    public JwtRepoEntity isTokenBlacklisted(String jwt) {
        log.info("Calling isTokenBlacklisted in tokenService");
        return blacklistTokenRepository.findById(jwt).orElse(null);
    }

    //unfortunately, we must define this here, otherwise we get circular references between tokenServ, memberServ and memberContrl.
    public Optional<String> getMemberIdByUsername(String username) {
        log.info("Calling getMemberIdByUsername in tokenService");
        Assert.hasText(username, "email cannot be empty");
        var member = memberRepository.findMemberEntityByUsername(username);
        return member.map(memberEntity -> Optional.ofNullable(String.valueOf(memberEntity.getId()))).orElse(null);
    }
}

package com.auth.authserver2.services.impl;


import com.auth.authserver2.domains.member.MemberEntity;
import com.auth.authserver2.domains.tokens.ConfirmationTokenEntity;
import com.auth.authserver2.exceptions.UnexpectedConfirmationTokenUpdateException;
import com.auth.authserver2.repositories.ConfirmationTokenRepository;
import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.services.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service("tokenService")
public class TokenServiceImpl implements TokenService {

    private JwtEncoder jwtEncoder;

    private MemberRepository memberRepository;

    private ConfirmationTokenRepository confirmationTokenRepository;


    @Autowired
    public TokenServiceImpl(JwtEncoder jwtEncoder, MemberRepository memberRepository, ConfirmationTokenRepository confirmationTokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.memberRepository = memberRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    public String generateJwt(Authentication auth) {
        Instant now = Instant.now();

        String scope = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String memberId = getMemberIdByUsername(auth.getName()).orElseThrow( () -> new RuntimeException("MemberId not found, check logs"));

        //todo : query DB and get more claims to add, getName() is = username from userdetails
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString()) //jti or id
                .issuer("http://localhost:8080") //who issued
                .issuedAt(now) //when it was issued
                .expiresAt(now.plusSeconds(1800)) //when it expires (30 min form issuing)
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
        return confirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public Optional<ConfirmationTokenEntity> getToken(String token) {
        return confirmationTokenRepository.findConfirmationTokenEntityByToken(token);
    }

    @Override
    public ConfirmationTokenEntity createConfirmationTokenEntity(MemberEntity memberEntity) {

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
        log.info("Accessing updateMemberConfirmationTokenWhenConfirmingAccount with token: {} ", token );
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
        var foundToken = confirmationTokenRepository.findConfirmationTokenEntityByToken(token);
        return foundToken.map(ConfirmationTokenEntity::getMemberEntity).orElse(null);
    }

    //unfortunately, we must define this here, otherwise we get circular references between tokenServ, memberServ and memberContrl.
    public Optional<String> getMemberIdByUsername(String username) {
        Assert.hasText(username, "email cannot be empty");
        var member = memberRepository.findMemberEntityByUsername(username);
        return member.map(memberEntity -> Optional.ofNullable(String.valueOf(memberEntity.getId()))).orElse(null);
    }
}

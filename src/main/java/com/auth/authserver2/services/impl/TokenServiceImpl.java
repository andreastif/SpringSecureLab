package com.auth.authserver2.services.impl;


import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("tokenService")
public class TokenServiceImpl implements TokenService {

    private JwtEncoder jwtEncoder;

    private MemberRepository memberRepository;


    @Autowired
    public TokenServiceImpl(JwtEncoder jwtEncoder, MemberRepository memberRepository) {
        this.jwtEncoder = jwtEncoder;
        this.memberRepository = memberRepository;
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

    public Optional<String> getMemberIdByUsername(String username) {
        Assert.hasText(username, "email cannot be empty");
        var member = memberRepository.findMemberEntityByUsername(username);
        return member.map(memberEntity -> Optional.ofNullable(String.valueOf(memberEntity.getId()))).orElse(null);
    }
}

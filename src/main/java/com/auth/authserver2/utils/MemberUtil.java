package com.auth.authserver2.utils;


import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberEntity;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Slf4j
public class MemberUtil {

    public static MemberEntity toNewEntity(MemberDto memberDto) {
        return MemberEntity
                .builder()
                .uuid(UUID.randomUUID())
                .username(memberDto.getUsername().toLowerCase())
                .email(memberDto.getEmail().toLowerCase())
                .created(Instant.now())
                .lastUpdated(Instant.now())
                //Password is NOT included for safety reasons
                .firstname(memberDto.getFirstname().toLowerCase())
                .lastname(memberDto.getLastname().toLowerCase())
                .registeredToClientId(memberDto.getRegisteredToClientId().toLowerCase())
                .accountNonExpired(memberDto.isAccountNonExpired())
                .accountNonLocked(memberDto.isAccountNonLocked())
                .credentialsNonExpired(memberDto.isCredentialsNonExpired())
                .enabled(memberDto.isEnabled())
                .build();
    }

    public static MemberEntity toExistingEntity(MemberDto memberDto) {

        return MemberEntity
                .builder()
                .username(memberDto.getUsername().toLowerCase())
                .email(memberDto.getEmail().toLowerCase())
                .lastUpdated(Instant.now())
                .password(memberDto.getPassword())
                .firstname(memberDto.getFirstname().toLowerCase())
                .lastname(memberDto.getLastname().toLowerCase())
                .registeredToClientId(memberDto.getRegisteredToClientId().toLowerCase())
                .accountNonExpired(memberDto.isAccountNonExpired())
                .accountNonLocked(memberDto.isAccountNonLocked())
                .credentialsNonExpired(memberDto.isCredentialsNonExpired())
                .enabled(memberDto.isEnabled())
                .build();
    }

    public static MemberDto toDto(MemberEntity memberEntity) {
        return MemberDto.builder()
                .username(memberEntity.getUsername())
                .email(memberEntity.getEmail())
                .firstname(memberEntity.getFirstname())
                .lastname(memberEntity.getLastname())
                .password(memberEntity.getPassword())
                .memberRoles(RoleUtil.toRoleSet(memberEntity.getMemberRoles()))
                .enabled(memberEntity.isEnabled())
                .accountNonExpired(memberEntity.isAccountNonExpired())
                .registeredToClientId(memberEntity.getRegisteredToClientId())
                .credentialsNonExpired(memberEntity.isCredentialsNonExpired())
                .accountNonLocked(memberEntity.isAccountNonLocked())
                .build();
    }


}

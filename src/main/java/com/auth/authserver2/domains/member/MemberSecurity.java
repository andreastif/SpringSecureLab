package com.auth.authserver2.domains.member;

import com.auth.authserver2.domains.roles.RolesSecurity;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;



@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSecurity implements UserDetails {

    private MemberEntity member;

    public Long getId() {
        return member.getId();
    }

    public UUID getUuid() {
       return member.getUuid();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    public String getEmail() {
        return member.getEmail();
    }

    public Instant getCreated() {
        return member.getCreated();
    }

    public Instant getLastUpdated() {
        return member.getLastUpdated();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    public String getFirstname() {
        return member.getFirstname();
    }

    public String getLastname() {
        return member.getLastname();
    }

    public String getRegisteredToClientId() {
        return member.getRegisteredToClientId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return member.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return member.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return member.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return member.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return member.getMemberRoles()
                .stream().map(role -> new RolesSecurity(role.getRoles()))
                .collect(Collectors.toSet());
    }
}

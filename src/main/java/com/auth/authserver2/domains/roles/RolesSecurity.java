package com.auth.authserver2.domains.roles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class RolesSecurity implements GrantedAuthority {

    private RoleEntity roleEntity;

    @Override
    public String getAuthority() {
        return roleEntity.getRoleName();
    }
}

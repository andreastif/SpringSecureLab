package com.auth.authserver2.domains.roles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
@Getter
@Setter
public class RolesSecurity implements GrantedAuthority {

    private RolesEntity rolesEntity;

    @Override
    public String getAuthority() {
        return rolesEntity.getRoleName();
    }
}

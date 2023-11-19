package com.auth.authserver2.domains.roles;

import org.springframework.security.core.GrantedAuthority;

public class RolesSecurity implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return null;
    }
}

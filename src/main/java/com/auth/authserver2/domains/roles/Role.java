package com.auth.authserver2.domains.roles;

import lombok.Getter;

@Getter
public enum Role {

    ROLE_NONE("ROLE_NONE"),
    ROLE_GUEST("ROLE_GUEST"),
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    public final String role;

    Role(String role) {
        this.role = role;
    }


    public static Role findByValue(String value) {
        Role result = null;
        for (Role theRole : values()) {
            if (theRole.getRole().equalsIgnoreCase(value)) {
                result = theRole;
                break;
            }
        }
        return result;
    }

    public static boolean exists(String value) {
        for (Role theRole : values()) {
            if (theRole.getRole().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}

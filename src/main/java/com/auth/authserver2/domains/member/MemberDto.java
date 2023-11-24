package com.auth.authserver2.domains.member;


import com.auth.authserver2.domains.roles.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private String firstname;
    private String lastname;
    private String registeredToClientId;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private Set<Role> memberRoles;

}

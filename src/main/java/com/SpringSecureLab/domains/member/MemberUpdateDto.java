package com.SpringSecureLab.domains.member;

import com.SpringSecureLab.domains.roles.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MemberUpdateDto {

    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;
    private String username;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String registeredToClientId;
    private Boolean accountNonExpired; //using Wrapper classes since if we dont send in boolean values, this equates to null (and not false) and no update issues are encountered
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private Boolean enabled;
    private Set<Role> memberRoles;

}

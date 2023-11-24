package com.auth.authserver2.domains.member;


import com.auth.authserver2.domains.roles.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    //think about serialization from the POV of the java object, you deserialize INTO a POJO, FROM JSON
    //and serialize OUT FROM a POJO, INTO JSON. It is easy to get confused because serialization and deserialization
    //happen at the same time, and depending on which object you think about one or the other is happening!
    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY) //deserializes INTO POJO only.
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

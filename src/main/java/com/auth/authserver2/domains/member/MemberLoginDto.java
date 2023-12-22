package com.auth.authserver2.domains.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MemberLoginDto {
    private String username;
    private String password;
}

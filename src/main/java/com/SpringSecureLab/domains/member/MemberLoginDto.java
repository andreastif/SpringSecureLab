package com.SpringSecureLab.domains.member;

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

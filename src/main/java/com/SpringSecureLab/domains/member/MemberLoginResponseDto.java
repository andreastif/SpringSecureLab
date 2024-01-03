package com.SpringSecureLab.domains.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MemberLoginResponseDto {
    private String roles;
    private Long expiryTimeMillis;

}

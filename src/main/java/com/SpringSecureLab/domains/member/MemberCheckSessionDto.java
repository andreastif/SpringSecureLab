package com.SpringSecureLab.domains.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MemberCheckSessionDto {

    private Boolean isLoggedIn;
    private Boolean isAdmin;
    private Long expiryTimeMillis;

}

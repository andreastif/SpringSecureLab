package com.auth.authserver2.domains.member;

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
    //todo: add boolean isExpired + expiry time for cookie

}

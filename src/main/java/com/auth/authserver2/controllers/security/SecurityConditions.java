package com.auth.authserver2.controllers.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityConditions {

    public String checkMemberId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return "hello";
    }
}

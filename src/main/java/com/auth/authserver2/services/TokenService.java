package com.auth.authserver2.services;

import org.springframework.security.core.Authentication;

public interface TokenService {

    String generateJwt(Authentication auth);
}

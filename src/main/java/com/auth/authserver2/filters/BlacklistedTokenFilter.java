package com.auth.authserver2.filters;

import com.auth.authserver2.domains.tokens.JwtRepoEntity;
import com.auth.authserver2.repositories.BlacklistTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class BlacklistedTokenFilter extends OncePerRequestFilter {

    private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    @Autowired
    private BlacklistTokenRepository repository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //<0> info
        //request.getHeader("Authorization") gets the bearer+token but does not do additional checks like the built-in resolver

        //<1> extract
        var token = bearerTokenResolver.resolve(request); //Springs built in default resolver, checks if malformed or otherwise non-standard.

        //<2> decide
        if (token != null && isTokenBlacklisted(token) != null) {
            log.info("Attempt to use a blacklisted token");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "This token is no longer valid.");
            return;
        }
        //<3> do
        filterChain.doFilter(request, response);

    }

    JwtRepoEntity isTokenBlacklisted(String token) {
        return repository.findById(token).orElse(null);
    }
}

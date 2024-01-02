package com.auth.authserver2.filters;

import com.auth.authserver2.utils.CryptoUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class DecryptTokenFilter extends OncePerRequestFilter {

    @Autowired
    private CryptoUtility cryptoUtility;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Attempting to decrypt JWT inside cookie");

        //<0> setup
        Cookie[] cookies = request.getCookies();

        //<1> decide, decrypt token and pass on in the filterchain
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("JWT_COOKIE")) {
                    String decryptedJwt = cryptoUtility.decrypt(cookie.getValue());
                    cookie.setValue(decryptedJwt);
                }
            }
        }

        //<2> do
        filterChain.doFilter(request, response);
    }


}

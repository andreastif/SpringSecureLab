package com.SpringSecureLab.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ExtractAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //<0> setup
        Cookie[] cookies = request.getCookies();

        //<1> decide & <2> extract & maybe <3> do
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("JWT_COOKIE")) {
                    String jwt = cookie.getValue();

                    // Set the JWT as an Authorization header
                    HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
                        @Override
                        public String getHeader(String name) {
                            if ("Authorization".equalsIgnoreCase(name)) {
                                return "Bearer " + jwt;
                            }
                            //default to normal behavior if the Authorization header is not found
                            // (which will ever happen at this point given the wrapper is called within this clause -> if (cookie.getName().equals("JWT_COOKIE")) )
                            return super.getHeader(name);
                        }
                    };
                    filterChain.doFilter(wrappedRequest, response);
                    return;
                }
            }
        }
        //<3> do, NO JWT, send down the filter chain
        filterChain.doFilter(request, response);

    }
}

package com.auth.authserver2.services.impl;

import com.auth.authserver2.services.MemberUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MemberUserDetailsServiceImpl implements MemberUserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return null;
    }

}

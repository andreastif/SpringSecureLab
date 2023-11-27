package com.auth.authserver2.services.impl;

import com.auth.authserver2.domains.member.MemberSecurity;
import com.auth.authserver2.repositories.MemberRepository;
import com.auth.authserver2.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements CustomUserDetailsService {

    MemberRepository memberRepository;

    @Autowired
    public UserDetailsServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.hasText(username, "username cannot be empty");

        return memberRepository
                .findMemberEntityByUsername(username)
                .map(MemberSecurity::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

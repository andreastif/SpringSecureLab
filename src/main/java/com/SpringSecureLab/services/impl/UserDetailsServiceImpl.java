package com.SpringSecureLab.services.impl;

import com.SpringSecureLab.repositories.MemberRepository;
import com.SpringSecureLab.services.CustomUserDetailsService;
import com.SpringSecureLab.domains.member.MemberSecurity;
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

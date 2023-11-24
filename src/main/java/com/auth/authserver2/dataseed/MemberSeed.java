package com.auth.authserver2.dataseed;

import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.services.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.auth.authserver2.domains.roles.Role.*;

@Component
@Slf4j
public class MemberSeed implements CommandLineRunner {




    @Autowired
    @Qualifier("memberService")
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {



       var member = MemberDto.builder()
                .username("mrUser")
                .email("andreas@gmail.com")
                .firstname("andreas")
                .lastname("tiflidis")
                .password(passwordEncoder.encode("test"))
                .registeredToClientId("auth_serv")
                .memberRoles(Set.of(ROLE_ADMIN, ROLE_USER, ROLE_GUEST, ROLE_NONE))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .enabled(true)
                .credentialsNonExpired(true)
                .build();

        var msg = memberService.save(member);
        log.info("MemberSeed: {}", msg);
    }


}

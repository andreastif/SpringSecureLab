package com.auth.authserver2.repositories;

import com.auth.authserver2.domains.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
}

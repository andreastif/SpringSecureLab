package com.auth.authserver2.repositories;

import com.auth.authserver2.domains.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findMemberEntityByUsername(String username);

    Optional<MemberEntity> findMemberEntityByEmail(String email);

    Optional<MemberEntity> findMemberEntityById(Long id);
    void deleteByEmail(String email);

    void deleteAllById(Long id);

}

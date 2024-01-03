package com.SpringSecureLab.repositories;

import com.SpringSecureLab.domains.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findMemberEntityByUsername(String username);

    Optional<MemberEntity> findMemberEntityByEmail(String email);

    Optional<MemberEntity> findMemberEntityById(Long id);
    void deleteByEmail(String email);
    void deleteAllById(Long id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = "UPDATE members SET enabled = true WHERE id =:id")
    int updateMemberEnabledById(Long id);

}

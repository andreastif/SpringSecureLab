package com.SpringSecureLab.repositories;

import com.SpringSecureLab.domains.tokens.ConfirmationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationTokenEntity, Long> {

    Optional<ConfirmationTokenEntity> findConfirmationTokenEntityByToken(String token);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true,
            value = "UPDATE confirmation_tokens SET confirmed_at =:confirmedAt WHERE token = :token")
    int updateConfirmationTokenEntity(@Param("token") String token, @Param("confirmedAt") Instant confirmedAt);
}
package com.auth.authserver2.repositories;

import com.auth.authserver2.domains.tokens.JwtRepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistTokenRepository extends JpaRepository<JwtRepoEntity, String> {
}

package com.SpringSecureLab.repositories;

import com.SpringSecureLab.domains.tokens.JwtRepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistTokenRepository extends JpaRepository<JwtRepoEntity, String> {
}

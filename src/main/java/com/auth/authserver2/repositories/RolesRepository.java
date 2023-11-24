package com.auth.authserver2.repositories;

import com.auth.authserver2.domains.roles.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<RolesEntity, Long> {
}

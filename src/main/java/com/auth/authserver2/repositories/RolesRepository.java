package com.auth.authserver2.repositories;

import com.auth.authserver2.domains.roles.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findRolesEntityByRoleName(String name);

}

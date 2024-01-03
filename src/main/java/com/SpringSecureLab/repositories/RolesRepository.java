package com.SpringSecureLab.repositories;

import com.SpringSecureLab.domains.roles.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findRolesEntityByRoleName(String name);

}

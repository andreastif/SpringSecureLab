package com.auth.authserver2.repositories;

import com.auth.authserver2.domains.map.MemberRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRoleMapRepository extends JpaRepository<MemberRoleEntity, Long> {
}

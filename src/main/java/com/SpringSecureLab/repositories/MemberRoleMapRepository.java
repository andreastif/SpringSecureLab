package com.SpringSecureLab.repositories;

import com.SpringSecureLab.domains.map.MemberRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRoleMapRepository extends JpaRepository<MemberRoleEntity, Long> {

    void deleteMemberRoleEntitiesByMemberId(Long id);

    void deleteAllByMemberId(Long id);
}

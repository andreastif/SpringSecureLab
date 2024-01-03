package com.SpringSecureLab.utils;

import com.SpringSecureLab.domains.map.MemberRoleEntity;
import com.SpringSecureLab.domains.roles.Role;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@Slf4j
public class RoleUtil {

   public static Set<Role> toRoleSet(Set<MemberRoleEntity> memberRoleEntity) {
       return memberRoleEntity.stream().map(roleEntity -> {
           if (Role.exists(roleEntity.getRoles().getRoleName())) {
               return Role.findByValue(roleEntity.getRoles().getRoleName());
           } else {
               throw new RuntimeException("role does not exist");
           }
       }).collect(Collectors.toSet());
   }

}

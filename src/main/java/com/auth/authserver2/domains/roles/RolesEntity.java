package com.auth.authserver2.domains.roles;

import com.auth.authserver2.domains.map.MemberRoleEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class RolesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName; // This maps to the Role enum's name.

    @OneToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Set<MemberRoleEntity> memberRoles;


}

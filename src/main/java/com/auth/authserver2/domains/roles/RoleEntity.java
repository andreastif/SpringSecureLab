package com.auth.authserver2.domains.roles;

import com.auth.authserver2.domains.map.MemberRoleEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "role_name", unique = true)
    private String roleName; // This maps to the Role enum's name.

    @OneToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    @JsonManagedReference
    private Set<MemberRoleEntity> memberRoles;


    public RoleEntity(String stringRole) {
        this.roleName = stringRole;
    }
}

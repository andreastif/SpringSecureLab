package com.auth.authserver2.domains.map;

import com.auth.authserver2.domains.member.MemberEntity;
import com.auth.authserver2.domains.roles.RoleEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_role_map")
public class MemberRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity roles;

    public MemberRoleEntity(RoleEntity role, MemberEntity member) {
        this.roles = role;
        this.member = member;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberRoleEntity that)) return false;

        if (!id.equals(that.id)) return false;
        if (!Objects.equals(member, that.member)) return false;
        return Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (member != null ? member.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }


}

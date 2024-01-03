package com.SpringSecureLab.domains.tokens;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "jwts")
public class JwtRepoEntity {

    @Id
    @Column(name = "jwt", nullable = false)
    private String jwt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}

package com.auth.authserver2.domains.tokens;

import com.auth.authserver2.domains.member.MemberEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "confirmation_tokens")
public class ConfirmationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name ="token", nullable = false)
    private String token;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name ="expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @ManyToOne
    @JoinColumn(
            nullable = false,
            name = "member_id"
    )
    private MemberEntity memberEntity;
}

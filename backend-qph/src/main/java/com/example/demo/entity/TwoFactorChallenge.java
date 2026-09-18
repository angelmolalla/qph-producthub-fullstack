package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "two_factor_challenges")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorChallenge {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private UserEntity user;

    @Column(
            name = "email_otp_hash",
            nullable = false,
            length = 128
    )
    private String emailOtpHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Boolean used;
}
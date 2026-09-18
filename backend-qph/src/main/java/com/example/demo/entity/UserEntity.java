package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @Column(
            unique = true,
            length = 255
    )
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(
            name = "two_factor_enabled",
            nullable = false
    )
    private Boolean twoFactorEnabled;

    @Column(
            name = "totp_secret",
            length = 128
    )
    private String totpSecret;
}
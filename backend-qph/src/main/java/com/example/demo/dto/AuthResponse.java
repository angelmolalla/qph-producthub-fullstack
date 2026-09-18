package com.example.demo.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;

    private boolean twoFactorRequired;

    private UUID challengeId;

    private Long expiresInSeconds;
}
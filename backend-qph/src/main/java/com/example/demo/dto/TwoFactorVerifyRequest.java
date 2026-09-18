package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TwoFactorVerifyRequest {

    @NotNull
    private UUID challengeId;
}
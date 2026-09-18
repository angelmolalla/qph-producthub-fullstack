package com.example.demo.service;

import java.util.UUID;

public interface OtpHashService {

    String hash(
            UUID challengeId,
            String otp
    );

    boolean matches(
            UUID challengeId,
            String otp,
            String expectedHash
    );
}

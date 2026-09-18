package com.example.demo.service;

import com.example.demo.entity.TwoFactorChallenge;
import com.example.demo.entity.TwoFactorType;
import com.example.demo.entity.UserEntity;

import java.util.UUID;

public interface TwoFactorService {

    TwoFactorChallenge createChallenge(
            UserEntity user
    );

    UserEntity verify(
            UUID challengeId,
            TwoFactorType type,
            String code
    );
}

package com.example.demo.service;

import com.example.demo.entity.UserEntity;

public interface EmailTemplateService {

    String accountCreated(
            UserEntity user
    );

    String emailOtp(
            UserEntity user,
            String otp
    );

    String authenticatorSetup(
            UserEntity user,
            String secret
    );
}

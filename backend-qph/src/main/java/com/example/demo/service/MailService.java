package com.example.demo.service;

import com.example.demo.entity.UserEntity;

public interface MailService {

    boolean sendAccountCreated(
            UserEntity user
    );

    boolean sendEmailOtp(
            UserEntity user,
            String otp
    );

    boolean sendAuthenticatorSetup(
            UserEntity user,
            String secret,
            byte[] qrImage
    );
}

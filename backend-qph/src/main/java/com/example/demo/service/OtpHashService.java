package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
public class OtpHashService {

    @Value("${app.2fa.otp-pepper}")
    private String pepper;

    public String hash(
            UUID challengeId,
            String otp) {

        try {

            Mac mac =
                    Mac.getInstance("HmacSHA256");

            mac.init(
                    new SecretKeySpec(
                            pepper.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    )
            );

            String value =
                    challengeId + ":" + otp;

            byte[] result =
                    mac.doFinal(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64
                    .getEncoder()
                    .encodeToString(result);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Error procesando OTP",
                    e
            );
        }
    }

    public boolean matches(
            UUID challengeId,
            String otp,
            String expectedHash) {

        String calculated =
                hash(challengeId, otp);

        return MessageDigest.isEqual(
                calculated.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
package com.example.demo.security;

import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

@Service
public class TotpService {

    private static final int SECRET_SIZE = 20;
    private static final int DIGITS = 6;
    private static final long PERIOD_SECONDS = 30;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value("${app.2fa.issuer}")
    private String issuer;

    public String generateSecret() {

        byte[] secret = new byte[SECRET_SIZE];

        secureRandom.nextBytes(secret);

        Base32 base32 = new Base32();

        return base32
                .encodeToString(secret)
                .replace("=", "");
    }

    public boolean verifyCode(
            String secret,
            String code) {

        if (secret == null
                || code == null
                || !code.matches("\\d{6}")) {

            return false;
        }

        long currentInterval =
                System.currentTimeMillis()
                        / 1000
                        / PERIOD_SECONDS;

        // Permitimos +-30 segundos por diferencia de reloj.
        for (long i = -1; i <= 1; i++) {

            String expected =
                    generateCode(
                            secret,
                            currentInterval + i
                    );

            if (MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    code.getBytes(StandardCharsets.UTF_8))) {

                return true;
            }
        }

        return false;
    }

    public String buildOtpAuthUri(
            String username,
            String secret) {

        String encodedIssuer =
                URLEncoder.encode(
                        issuer,
                        StandardCharsets.UTF_8
                );

        String encodedAccount =
                URLEncoder.encode(
                        username,
                        StandardCharsets.UTF_8
                );

        return "otpauth://totp/"
                + encodedIssuer
                + ":"
                + encodedAccount
                + "?secret="
                + secret
                + "&issuer="
                + encodedIssuer
                + "&algorithm=SHA1"
                + "&digits=6"
                + "&period=30";
    }

    private String generateCode(
            String secret,
            long counter) {

        try {

            Base32 base32 =
                    new Base32();

            byte[] key =
                    base32.decode(secret);

            byte[] data =
                    ByteBuffer
                            .allocate(8)
                            .putLong(counter)
                            .array();

            Mac mac =
                    Mac.getInstance("HmacSHA1");

            mac.init(
                    new SecretKeySpec(
                            key,
                            "HmacSHA1"
                    )
            );

            byte[] hash =
                    mac.doFinal(data);

            int offset =
                    hash[hash.length - 1]
                            & 0x0F;

            int binary =
                    ((hash[offset] & 0x7F) << 24)
                            | ((hash[offset + 1] & 0xFF) << 16)
                            | ((hash[offset + 2] & 0xFF) << 8)
                            | (hash[offset + 3] & 0xFF);

            int otp =
                    binary % 1_000_000;

            return String.format(
                    "%06d",
                    otp
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Error generando TOTP",
                    e
            );
        }
    }
}
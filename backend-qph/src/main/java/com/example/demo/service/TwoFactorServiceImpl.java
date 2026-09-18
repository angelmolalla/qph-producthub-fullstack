package com.example.demo.service;

import com.example.demo.entity.TwoFactorChallenge;
import com.example.demo.entity.TwoFactorType;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.TwoFactorAuthenticationException;
import com.example.demo.repository.TwoFactorChallengeRepository;
import com.example.demo.security.TotpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorServiceImpl
        implements TwoFactorService {

    private final TwoFactorChallengeRepository challengeRepository;
    private final OtpHashService otpHashService;
    private final TotpService totpService;
    private final MailService mailService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value("${app.2fa.expiration-seconds:300}")
    private long expirationSeconds;

    @Value("${app.2fa.max-attempts:3}")
    private int maxAttempts;

    @Override
    @Transactional
    public TwoFactorChallenge createChallenge(
            UserEntity user) {

        challengeRepository
                .deleteByUser_Id(
                        user.getId()
                );

        UUID challengeId =
                UUID.randomUUID();

        String emailOtp =
                generateEmailOtp();

        TwoFactorChallenge challenge =
                TwoFactorChallenge.builder()
                        .id(challengeId)
                        .user(user)
                        .emailOtpHash(
                                otpHashService.hash(
                                        challengeId,
                                        emailOtp
                                )
                        )
                        .expiresAt(
                                Instant.now()
                                        .plusSeconds(
                                                expirationSeconds
                                        )
                        )
                        .attempts(0)
                        .used(false)
                        .build();

        challengeRepository.save(
                challenge
        );

        boolean emailSent =
                mailService.sendEmailOtp(
                        user,
                        emailOtp
                );

        if (!emailSent) {
            log.warn(
                    "No se pudo enviar OTP por correo al usuario '{}'. "
                            + "El challenge continúa válido para TOTP.",
                    user.getUsername()
            );
        }

        return challenge;
    }

    @Override
    @Transactional
    public UserEntity verify(
            UUID challengeId,
            TwoFactorType type,
            String code) {

        TwoFactorChallenge challenge =
                challengeRepository
                        .findById(
                                challengeId
                        )
                        .orElseThrow(() ->
                                new TwoFactorAuthenticationException(
                                        "Challenge 2FA inválido"
                                )
                        );

        validateChallenge(
                challenge
        );

        boolean valid;

        switch (type) {

            case EMAIL_AUTH ->
                    valid =
                            otpHashService.matches(
                                    challenge.getId(),
                                    code,
                                    challenge.getEmailOtpHash()
                            );

            case TOTP_AUTH -> {

                String secret =
                        challenge
                                .getUser()
                                .getTotpSecret();

                valid =
                        totpService.verifyCode(
                                secret,
                                code
                        );
            }

            default ->
                    valid = false;
        }

        if (!valid) {

            int attempts =
                    challenge.getAttempts()
                            + 1;

            challenge.setAttempts(
                    attempts
            );

            if (attempts
                    >= maxAttempts) {

                challenge.setUsed(
                        true
                );
            }

            challengeRepository.save(
                    challenge
            );

            throw new TwoFactorAuthenticationException(
                    "Código 2FA incorrecto. Intentos restantes: "
                            + Math.max(
                            0,
                            maxAttempts
                                    - attempts
                    )
            );
        }

        challenge.setUsed(
                true
        );

        challengeRepository.save(
                challenge
        );

        return challenge.getUser();
    }

    private void validateChallenge(
            TwoFactorChallenge challenge) {

        if (Boolean.TRUE.equals(
                challenge.getUsed())) {

            throw new TwoFactorAuthenticationException(
                    "El challenge 2FA ya fue utilizado"
            );
        }

        if (challenge
                .getExpiresAt()
                .isBefore(
                        Instant.now()
                )) {

            throw new TwoFactorAuthenticationException(
                    "El código 2FA ha expirado"
            );
        }

        if (challenge.getAttempts()
                >= maxAttempts) {

            throw new TwoFactorAuthenticationException(
                    "Se excedió el número máximo de intentos"
            );
        }

        if (!Boolean.TRUE.equals(
                challenge
                        .getUser()
                        .getEnabled())) {

            throw new TwoFactorAuthenticationException(
                    "Usuario deshabilitado"
            );
        }
    }

    private String generateEmailOtp() {

        return String.format(
                "%06d",
                secureRandom.nextInt(
                        1_000_000
                )
        );
    }
}

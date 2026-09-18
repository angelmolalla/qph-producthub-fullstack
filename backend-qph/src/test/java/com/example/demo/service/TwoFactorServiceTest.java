package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.TwoFactorChallenge;
import com.example.demo.entity.TwoFactorType;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.TwoFactorAuthenticationException;
import com.example.demo.repository.TwoFactorChallengeRepository;
import com.example.demo.security.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceTest {

    @Mock
    private TwoFactorChallengeRepository challengeRepository;

    @Mock
    private OtpHashService otpHashService;

    @Mock
    private TotpService totpService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private TwoFactorService service;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                service,
                "expirationSeconds",
                300L
        );

        ReflectionTestUtils.setField(
                service,
                "maxAttempts",
                3
        );
    }

    @Test
    void shouldCreateChallengeAndSendEmailOtp() {

        UserEntity user =
                user();

        when(
                otpHashService.hash(
                        any(UUID.class),
                        anyString()
                )
        )
                .thenReturn(
                        "HASH"
                );

        when(
                mailService.sendEmailOtp(
                        eq(user),
                        anyString()
                )
        )
                .thenReturn(true);

        TwoFactorChallenge challenge =
                service.createChallenge(
                        user
                );

        assertThat(
                challenge.getUser()
        ).isEqualTo(user);

        assertThat(
                challenge.getEmailOtpHash()
        ).isEqualTo("HASH");

        assertThat(
                challenge.getAttempts()
        ).isZero();

        assertThat(
                challenge.getUsed()
        ).isFalse();

        ArgumentCaptor<String> otpCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(mailService)
                .sendEmailOtp(
                        eq(user),
                        otpCaptor.capture()
                );

        assertThat(
                otpCaptor.getValue()
        )
                .matches("\\d{6}");
    }

    @Test
    void shouldVerifyEmailOtp() {

        UUID challengeId =
                UUID.randomUUID();

        UserEntity user =
                user();

        TwoFactorChallenge challenge =
                challenge(
                        challengeId,
                        user
                );

        when(
                challengeRepository.findById(
                        challengeId
                )
        )
                .thenReturn(
                        Optional.of(challenge)
                );

        when(
                otpHashService.matches(
                        challengeId,
                        "123456",
                        "HASH"
                )
        )
                .thenReturn(true);

        UserEntity result =
                service.verify(
                        challengeId,
                        TwoFactorType.EMAIL_AUTH,
                        "123456"
                );

        assertThat(result)
                .isEqualTo(user);

        assertThat(
                challenge.getUsed()
        ).isTrue();

        verify(challengeRepository)
                .save(challenge);
    }

    @Test
    void shouldIncreaseAttemptsWhenOtpIsInvalid() {

        UUID challengeId =
                UUID.randomUUID();

        TwoFactorChallenge challenge =
                challenge(
                        challengeId,
                        user()
                );

        when(
                challengeRepository.findById(
                        challengeId
                )
        )
                .thenReturn(
                        Optional.of(challenge)
                );

        when(
                otpHashService.matches(
                        any(),
                        anyString(),
                        anyString()
                )
        )
                .thenReturn(false);

        assertThatThrownBy(
                () ->
                        service.verify(
                                challengeId,
                                TwoFactorType.EMAIL_AUTH,
                                "000000"
                        )
        )
                .isInstanceOf(
                        TwoFactorAuthenticationException.class
                )
                .hasMessageContaining(
                        "Intentos restantes: 2"
                );

        assertThat(
                challenge.getAttempts()
        ).isEqualTo(1);

        verify(challengeRepository)
                .save(challenge);
    }

    private UserEntity user() {

        return UserEntity.builder()
                .id(1L)
                .username("angelo")
                .email("angelo@example.com")
                .password("encoded")
                .role(Role.USER)
                .enabled(true)
                .twoFactorEnabled(true)
                .totpSecret("SECRET")
                .build();
    }

    private TwoFactorChallenge challenge(
            UUID id,
            UserEntity user) {

        return TwoFactorChallenge.builder()
                .id(id)
                .user(user)
                .emailOtpHash("HASH")
                .expiresAt(
                        Instant.now()
                                .plusSeconds(300)
                )
                .attempts(0)
                .used(false)
                .build();
    }
}

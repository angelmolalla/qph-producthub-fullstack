package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OtpHashServiceTest {

    private OtpHashServiceImpl service;

    @BeforeEach
    void setUp() {

        service =
                new OtpHashServiceImpl();

        ReflectionTestUtils.setField(
                service,
                "pepper",
                "test-pepper"
        );
    }

    @Test
    void shouldMatchCorrectOtp() {

        UUID challengeId =
                UUID.randomUUID();

        String hash =
                service.hash(
                        challengeId,
                        "123456"
                );

        assertThat(
                service.matches(
                        challengeId,
                        "123456",
                        hash
                )
        )
                .isTrue();
    }

    @Test
    void shouldRejectWrongOtp() {

        UUID challengeId =
                UUID.randomUUID();

        String hash =
                service.hash(
                        challengeId,
                        "123456"
                );

        assertThat(
                service.matches(
                        challengeId,
                        "654321",
                        hash
                )
        )
                .isFalse();
    }
}

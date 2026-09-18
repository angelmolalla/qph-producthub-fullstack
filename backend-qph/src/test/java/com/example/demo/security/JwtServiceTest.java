package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String secret = Base64.getEncoder()
                .encodeToString("012345678901234567890123456789012345678901234567".getBytes(StandardCharsets.UTF_8));

        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);

        userDetails = User.withUsername("angelo")
                .password("encoded")
                .roles("USER")
                .build();
    }

    @Test
    void shouldGenerateAndExtractUsername() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("angelo");
    }

    @Test
    void shouldValidateTokenForSameUser() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails other = User.withUsername("other")
                .password("encoded")
                .roles("USER")
                .build();

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }
}

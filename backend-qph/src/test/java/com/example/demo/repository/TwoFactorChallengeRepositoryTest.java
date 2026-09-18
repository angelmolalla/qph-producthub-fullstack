package com.example.demo.repository;

import com.example.demo.entity.Role;
import com.example.demo.entity.TwoFactorChallenge;
import com.example.demo.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class TwoFactorChallengeRepositoryTest {

    @Autowired
    private TwoFactorChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldDeleteChallengesByUserId() {
        UserEntity user = userRepository.saveAndFlush(UserEntity.builder()
                .username("angelo")
                .email("angelo@example.com")
                .password("encoded")
                .role(Role.USER)
                .enabled(true)
                .twoFactorEnabled(true)
                .totpSecret("SECRET")
                .build());

        challengeRepository.saveAndFlush(TwoFactorChallenge.builder()
                .id(UUID.randomUUID())
                .user(user)
                .emailOtpHash("HASH")
                .expiresAt(Instant.now().plusSeconds(300))
                .attempts(0)
                .used(false)
                .build());

        challengeRepository.deleteByUser_Id(user.getId());
        challengeRepository.flush();

        assertThat(challengeRepository.findAll()).isEmpty();
    }
}

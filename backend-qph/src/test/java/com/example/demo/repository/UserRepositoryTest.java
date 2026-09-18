package com.example.demo.repository;

import com.example.demo.entity.Role;
import com.example.demo.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = repository.saveAndFlush(UserEntity.builder()
                .username("angelo")
                .email("angelo@example.com")
                .password("encoded")
                .role(Role.USER)
                .enabled(true)
                .twoFactorEnabled(false)
                .build());
    }

    @Test
    void shouldFindByUsername() {
        assertThat(repository.findByUsername("angelo"))
                .isPresent()
                .get()
                .extracting(UserEntity::getEmail)
                .isEqualTo("angelo@example.com");
    }

    @Test
    void shouldCheckUsernameAndEmailExistence() {
        assertThat(repository.existsByUsername("angelo")).isTrue();
        assertThat(repository.existsByEmail("angelo@example.com")).isTrue();
        assertThat(repository.existsByUsername("missing")).isFalse();
    }

    @Test
    void shouldIgnoreCurrentUserWhenCheckingUsername() {
        assertThat(repository.existsByUsernameAndIdNot("angelo", user.getId())).isFalse();
        assertThat(repository.existsByUsernameAndIdNot("angelo", user.getId() + 1)).isTrue();
    }

    @Test
    void shouldIgnoreCurrentUserWhenCheckingEmail() {
        assertThat(repository.existsByEmailAndIdNot("angelo@example.com", user.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("angelo@example.com", user.getId() + 1)).isTrue();
    }
}

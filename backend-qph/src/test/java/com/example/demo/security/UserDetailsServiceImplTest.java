package com.example.demo.security;

import com.example.demo.entity.Role;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    void shouldLoadUserWithRole() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user(true)));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void shouldMapDisabledUser() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user(false)));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado: missing");
    }

    private UserEntity user(boolean enabled) {
        return UserEntity.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .password("encoded")
                .role(Role.ADMIN)
                .enabled(enabled)
                .twoFactorEnabled(false)
                .build();
    }
}

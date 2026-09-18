package com.example.demo.config;

import com.example.demo.entity.Role;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateAdminWhenItDoesNotExist() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-admin");
        UserDataInitializer initializer = new UserDataInitializer(userRepository, passwordEncoder);
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

        initializer.run();

        verify(userRepository).save(captor.capture());
        UserEntity admin = captor.getValue();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getPassword()).isEqualTo("encoded-admin");
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getEnabled()).isTrue();
        assertThat(admin.getTwoFactorEnabled()).isFalse();
    }

    @Test
    void shouldNotCreateAdminWhenItAlreadyExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        UserDataInitializer initializer = new UserDataInitializer(userRepository, passwordEncoder);

        initializer.run();

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }
}

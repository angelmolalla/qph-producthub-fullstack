package com.example.demo.config;

import com.example.demo.entity.Role;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByUsername("admin")) {
            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(
                            passwordEncoder.encode(
                                    "admin123"
                            )
                    )
                    .email("producthub194@gmail.com")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .twoFactorEnabled(false)
                    .totpSecret(null)
                    .build();
            userRepository.save(admin);
            System.out.println(
                    "Usuario admin creado correctamente"
            );
        }
    }
}
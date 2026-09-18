package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.QrCodeService;
import com.example.demo.security.TotpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl
        implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;
    private final MailService mailService;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {

        return userRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(
            Long id) {

        return toResponse(
                findEntity(id)
        );
    }

    @Override
    @Transactional
    public UserResponse create(
            CreateUserRequest request) {

        if (userRepository
                .existsByUsername(
                        request.getUsername())) {

            throw new IllegalArgumentException(
                    "El username ya existe"
            );
        }

        if (userRepository
                .existsByEmail(
                        request.getEmail())) {

            throw new IllegalArgumentException(
                    "El email ya existe"
            );
        }

        String totpSecret = null;

        if (Boolean.TRUE.equals(
                request.getTwoFactorEnabled())) {

            totpSecret =
                    totpService.generateSecret();
        }

        UserEntity user =
                UserEntity.builder()
                        .username(
                                request.getUsername()
                        )
                        .email(
                                request.getEmail()
                        )
                        .password(
                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )
                        .role(
                                request.getRole()
                        )
                        .enabled(true)
                        .twoFactorEnabled(
                                request.getTwoFactorEnabled()
                        )
                        .totpSecret(
                                totpSecret
                        )
                        .build();

        user =
                userRepository.save(user);

        /*
         * El correo es una notificación.
         * Un fallo SMTP no revierte la creación
         * del usuario porque MailService no
         * propaga la excepción.
         */
        mailService
                .sendAccountCreated(user);

        if (Boolean.TRUE.equals(
                user.getTwoFactorEnabled())) {

            sendAuthenticatorSetup(user);
        }

        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse update(
            Long id,
            UpdateUserRequest request) {

        UserEntity user =
                findEntity(id);

        if (userRepository
                .existsByUsernameAndIdNot(
                        request.getUsername(),
                        id)) {

            throw new IllegalArgumentException(
                    "El username ya existe"
            );
        }

        if (userRepository
                .existsByEmailAndIdNot(
                        request.getEmail(),
                        id)) {

            throw new IllegalArgumentException(
                    "El email ya existe"
            );
        }

        boolean was2FaEnabled =
                Boolean.TRUE.equals(
                        user.getTwoFactorEnabled()
                );

        boolean will2FaEnabled =
                Boolean.TRUE.equals(
                        request.getTwoFactorEnabled()
                );

        user.setUsername(
                request.getUsername()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setRole(
                request.getRole()
        );

        user.setEnabled(
                request.getEnabled()
        );

        if (request.getPassword() != null
                && !request.getPassword()
                .isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        if (!was2FaEnabled
                && will2FaEnabled) {

            user.setTotpSecret(
                    totpService.generateSecret()
            );
        }

        if (was2FaEnabled
                && !will2FaEnabled) {

            user.setTotpSecret(null);
        }

        user.setTwoFactorEnabled(
                will2FaEnabled
        );

        user =
                userRepository.save(user);

        if (!was2FaEnabled
                && will2FaEnabled) {

            sendAuthenticatorSetup(user);
        }

        return toResponse(user);
    }

    @Override
    @Transactional
    public void delete(
            Long id) {

        UserEntity user =
                findEntity(id);

        userRepository.delete(user);
    }

    private void sendAuthenticatorSetup(
            UserEntity user) {

        String otpAuthUri =
                totpService.buildOtpAuthUri(
                        user.getUsername(),
                        user.getTotpSecret()
                );

        byte[] qr =
                qrCodeService.generateQrCode(
                        otpAuthUri
                );

        mailService
                .sendAuthenticatorSetup(
                        user,
                        user.getTotpSecret(),
                        qr
                );
    }

    private UserEntity findEntity(
            Long id) {

        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado: "
                                        + id
                        )
                );
    }

    private UserResponse toResponse(
            UserEntity user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(
                        user.getUsername()
                )
                .email(
                        user.getEmail()
                )
                .role(
                        user.getRole()
                )
                .enabled(
                        user.getEnabled()
                )
                .twoFactorEnabled(
                        user.getTwoFactorEnabled()
                )
                .build();
    }
}
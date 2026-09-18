package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.QrCodeService;
import com.example.demo.security.TotpService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TotpService totpService;

    @Mock
    private MailService mailService;

    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private UserServiceImpl service;


    // =========================================================
    // FIND ALL
    // =========================================================

    @Test
    void shouldReturnAllUsers() {

        UserEntity user1 =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        UserEntity user2 =
                user(
                        2L,
                        "admin",
                        "admin@example.com",
                        Role.ADMIN,
                        true,
                        true,
                        "SECRET"
                );

        when(
                userRepository.findAll()
        ).thenReturn(
                List.of(
                        user1,
                        user2
                )
        );


        List<UserResponse> response =
                service.findAll();


        assertThat(response)
                .hasSize(2);

        assertThat(
                response.get(0)
                        .getUsername()
        ).isEqualTo(
                "angelo"
        );

        assertThat(
                response.get(1)
                        .getRole()
        ).isEqualTo(
                Role.ADMIN
        );

        assertThat(
                response.get(1)
                        .getTwoFactorEnabled()
        ).isTrue();


        verify(
                userRepository
        ).findAll();
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    @Test
    void shouldFindUserById() {

        UserEntity user =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(user)
        );


        UserResponse response =
                service.findById(1L);


        assertThat(
                response.getId()
        ).isEqualTo(
                1L
        );

        assertThat(
                response.getUsername()
        ).isEqualTo(
                "angelo"
        );

        assertThat(
                response.getEmail()
        ).isEqualTo(
                "angelo@example.com"
        );

        assertThat(
                response.getEnabled()
        ).isTrue();
    }


    @Test
    void shouldThrowWhenFindingNonExistingUser() {

        when(
                userRepository.findById(99L)
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(
                () ->
                        service.findById(99L)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Usuario no encontrado: 99"
                );
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void shouldCreateUserWithout2Fa() {

        CreateUserRequest request =
                createRequest(false);

        when(
                passwordEncoder.encode(
                        "Mono96mh"
                )
        ).thenReturn(
                "encoded-password"
        );

        when(
                userRepository.save(
                        any(UserEntity.class)
                )
        ).thenAnswer(
                invocation -> {

                    UserEntity user =
                            invocation.getArgument(0);

                    user.setId(1L);

                    return user;
                }
        );


        UserResponse response =
                service.create(request);


        assertThat(
                response.getId()
        ).isEqualTo(
                1L
        );

        assertThat(
                response.getUsername()
        ).isEqualTo(
                "angelo"
        );

        assertThat(
                response.getTwoFactorEnabled()
        ).isFalse();


        verify(
                passwordEncoder
        ).encode(
                "Mono96mh"
        );

        verify(
                mailService
        ).sendAccountCreated(
                any(UserEntity.class)
        );

        verify(
                totpService,
                never()
        ).generateSecret();

        verify(
                mailService,
                never()
        ).sendAuthenticatorSetup(
                any(),
                anyString(),
                any()
        );
    }


    @Test
    void shouldCreateUserWith2FaAndSendQr() {

        CreateUserRequest request =
                createRequest(true);

        when(
                passwordEncoder.encode(
                        "Mono96mh"
                )
        ).thenReturn(
                "encoded-password"
        );

        when(
                totpService.generateSecret()
        ).thenReturn(
                "TOTPSECRET"
        );

        when(
                userRepository.save(
                        any(UserEntity.class)
                )
        ).thenAnswer(
                invocation -> {

                    UserEntity user =
                            invocation.getArgument(0);

                    user.setId(1L);

                    return user;
                }
        );

        when(
                totpService.buildOtpAuthUri(
                        "angelo",
                        "TOTPSECRET"
                )
        ).thenReturn(
                "otpauth://qph"
        );

        byte[] qr =
                new byte[]{
                        1,
                        2,
                        3
                };

        when(
                qrCodeService.generateQrCode(
                        "otpauth://qph"
                )
        ).thenReturn(
                qr
        );


        UserResponse response =
                service.create(request);


        assertThat(
                response.getTwoFactorEnabled()
        ).isTrue();


        verify(
                totpService
        ).generateSecret();

        verify(
                totpService
        ).buildOtpAuthUri(
                "angelo",
                "TOTPSECRET"
        );

        verify(
                qrCodeService
        ).generateQrCode(
                "otpauth://qph"
        );

        verify(
                mailService
        ).sendAuthenticatorSetup(
                any(UserEntity.class),
                eq("TOTPSECRET"),
                same(qr)
        );
    }


    @Test
    void shouldRejectDuplicateUsername() {

        when(
                userRepository.existsByUsername(
                        "angelo"
                )
        ).thenReturn(
                true
        );


        assertThatThrownBy(
                () ->
                        service.create(
                                createRequest(false)
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El username ya existe"
                );


        verify(
                userRepository,
                never()
        ).save(
                any()
        );
    }


    @Test
    void shouldRejectDuplicateEmail() {

        when(
                userRepository.existsByUsername(
                        "angelo"
                )
        ).thenReturn(
                false
        );

        when(
                userRepository.existsByEmail(
                        "angelo@example.com"
                )
        ).thenReturn(
                true
        );


        assertThatThrownBy(
                () ->
                        service.create(
                                createRequest(false)
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El email ya existe"
                );


        verify(
                userRepository,
                never()
        ).save(
                any()
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void shouldUpdateBasicUserInformation() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                userRepository.save(existing)
        ).thenReturn(
                existing
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo2",
                        "angelo2@example.com",
                        null,
                        Role.ADMIN,
                        false,
                        false
                );


        UserResponse response =
                service.update(
                        1L,
                        request
                );


        assertThat(
                response.getUsername()
        ).isEqualTo(
                "angelo2"
        );

        assertThat(
                response.getEmail()
        ).isEqualTo(
                "angelo2@example.com"
        );

        assertThat(
                response.getRole()
        ).isEqualTo(
                Role.ADMIN
        );

        assertThat(
                response.getEnabled()
        ).isFalse();


        verify(
                userRepository
        ).save(
                existing
        );
    }


    @Test
    void shouldUpdatePasswordWhenProvided() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                passwordEncoder.encode(
                        "NuevaClave123"
                )
        ).thenReturn(
                "new-encoded-password"
        );

        when(
                userRepository.save(existing)
        ).thenReturn(
                existing
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo",
                        "angelo@example.com",
                        "NuevaClave123",
                        Role.USER,
                        true,
                        false
                );


        service.update(
                1L,
                request
        );


        assertThat(
                existing.getPassword()
        ).isEqualTo(
                "new-encoded-password"
        );


        verify(
                passwordEncoder
        ).encode(
                "NuevaClave123"
        );
    }


    @Test
    void shouldNotUpdatePasswordWhenPasswordIsNull() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        String originalPassword =
                existing.getPassword();

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                userRepository.save(existing)
        ).thenReturn(
                existing
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo",
                        "angelo@example.com",
                        null,
                        Role.USER,
                        true,
                        false
                );


        service.update(
                1L,
                request
        );


        assertThat(
                existing.getPassword()
        ).isEqualTo(
                originalPassword
        );


        verify(
                passwordEncoder,
                never()
        ).encode(
                anyString()
        );
    }


    @Test
    void shouldNotUpdatePasswordWhenPasswordIsBlank() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        String originalPassword =
                existing.getPassword();

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                userRepository.save(existing)
        ).thenReturn(
                existing
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo",
                        "angelo@example.com",
                        "   ",
                        Role.USER,
                        true,
                        false
                );


        service.update(
                1L,
                request
        );


        assertThat(
                existing.getPassword()
        ).isEqualTo(
                originalPassword
        );


        verify(
                passwordEncoder,
                never()
        ).encode(
                anyString()
        );
    }


    // =========================================================
    // UPDATE DUPLICADOS
    // =========================================================

    @Test
    void shouldRejectDuplicateUsernameDuringUpdate() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                userRepository
                        .existsByUsernameAndIdNot(
                                "usuarioExistente",
                                1L
                        )
        ).thenReturn(
                true
        );

        UpdateUserRequest request =
                updateRequest(
                        "usuarioExistente",
                        "angelo@example.com",
                        null,
                        Role.USER,
                        true,
                        false
                );


        assertThatThrownBy(
                () ->
                        service.update(
                                1L,
                                request
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El username ya existe"
                );


        verify(
                userRepository,
                never()
        ).save(
                any()
        );
    }


    @Test
    void shouldRejectDuplicateEmailDuringUpdate() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                userRepository
                        .existsByEmailAndIdNot(
                                "ocupado@example.com",
                                1L
                        )
        ).thenReturn(
                true
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo",
                        "ocupado@example.com",
                        null,
                        Role.USER,
                        true,
                        false
                );


        assertThatThrownBy(
                () ->
                        service.update(
                                1L,
                                request
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El email ya existe"
                );


        verify(
                userRepository,
                never()
        ).save(
                any()
        );
    }


    // =========================================================
    // UPDATE 2FA
    // =========================================================

    @Test
    void shouldEnable2FaDuringUpdate() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                totpService.generateSecret()
        ).thenReturn(
                "NEWSECRET"
        );

        when(
                userRepository.save(existing)
        ).thenReturn(
                existing
        );

        when(
                totpService.buildOtpAuthUri(
                        "angelo",
                        "NEWSECRET"
                )
        ).thenReturn(
                "otpauth://new"
        );

        byte[] qr =
                new byte[]{
                        7,
                        8,
                        9
                };

        when(
                qrCodeService.generateQrCode(
                        "otpauth://new"
                )
        ).thenReturn(
                qr
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo",
                        "angelo@example.com",
                        null,
                        Role.USER,
                        true,
                        true
                );


        UserResponse response =
                service.update(
                        1L,
                        request
                );


        assertThat(
                response.getTwoFactorEnabled()
        ).isTrue();

        assertThat(
                existing.getTotpSecret()
        ).isEqualTo(
                "NEWSECRET"
        );


        verify(
                totpService
        ).generateSecret();

        verify(
                mailService
        ).sendAuthenticatorSetup(
                existing,
                "NEWSECRET",
                qr
        );
    }


    @Test
    void shouldDisable2FaDuringUpdate() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        true,
                        "OLDSECRET"
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                userRepository.save(existing)
        ).thenReturn(
                existing
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo",
                        "angelo@example.com",
                        null,
                        Role.USER,
                        true,
                        false
                );


        UserResponse response =
                service.update(
                        1L,
                        request
                );


        assertThat(
                response.getTwoFactorEnabled()
        ).isFalse();

        assertThat(
                existing.getTotpSecret()
        ).isNull();


        verify(
                totpService,
                never()
        ).generateSecret();

        verify(
                mailService,
                never()
        ).sendAuthenticatorSetup(
                any(),
                anyString(),
                any()
        );
    }


    @Test
    void shouldKeepExistingSecretWhen2FaRemainsEnabled() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        true,
                        "CURRENTSECRET"
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                userRepository.save(existing)
        ).thenReturn(
                existing
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo",
                        "angelo@example.com",
                        null,
                        Role.USER,
                        true,
                        true
                );


        service.update(
                1L,
                request
        );


        assertThat(
                existing.getTotpSecret()
        ).isEqualTo(
                "CURRENTSECRET"
        );


        verify(
                totpService,
                never()
        ).generateSecret();

        verify(
                mailService,
                never()
        ).sendAuthenticatorSetup(
                any(),
                anyString(),
                any()
        );
    }


    // =========================================================
    // UPDATE USER NOT FOUND
    // =========================================================

    @Test
    void shouldThrowWhenUpdatingNonExistingUser() {

        when(
                userRepository.findById(99L)
        ).thenReturn(
                Optional.empty()
        );

        UpdateUserRequest request =
                updateRequest(
                        "angelo",
                        "angelo@example.com",
                        null,
                        Role.USER,
                        true,
                        false
                );


        assertThatThrownBy(
                () ->
                        service.update(
                                99L,
                                request
                        )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Usuario no encontrado: 99"
                );


        verify(
                userRepository,
                never()
        ).save(
                any()
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldDeleteExistingUser() {

        UserEntity existing =
                user(
                        1L,
                        "angelo",
                        "angelo@example.com",
                        Role.USER,
                        true,
                        false,
                        null
                );

        when(
                userRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );


        service.delete(1L);


        verify(
                userRepository
        ).delete(
                existing
        );
    }


    @Test
    void shouldThrowWhenDeletingNonExistingUser() {

        when(
                userRepository.findById(99L)
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(
                () ->
                        service.delete(99L)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Usuario no encontrado: 99"
                );


        verify(
                userRepository,
                never()
        ).delete(
                any()
        );
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private CreateUserRequest createRequest(
            boolean twoFactorEnabled) {

        CreateUserRequest request =
                new CreateUserRequest();

        request.setUsername(
                "angelo"
        );

        request.setEmail(
                "angelo@example.com"
        );

        request.setPassword(
                "Mono96mh"
        );

        request.setRole(
                Role.USER
        );

        request.setTwoFactorEnabled(
                twoFactorEnabled
        );

        return request;
    }


    private UpdateUserRequest updateRequest(
            String username,
            String email,
            String password,
            Role role,
            Boolean enabled,
            Boolean twoFactorEnabled) {

        UpdateUserRequest request =
                new UpdateUserRequest();

        request.setUsername(
                username
        );

        request.setEmail(
                email
        );

        request.setPassword(
                password
        );

        request.setRole(
                role
        );

        request.setEnabled(
                enabled
        );

        request.setTwoFactorEnabled(
                twoFactorEnabled
        );

        return request;
    }


    private UserEntity user(
            Long id,
            String username,
            String email,
            Role role,
            Boolean enabled,
            Boolean twoFactorEnabled,
            String totpSecret) {

        return UserEntity.builder()
                .id(id)
                .username(username)
                .email(email)
                .password(
                        "encoded-password"
                )
                .role(role)
                .enabled(enabled)
                .twoFactorEnabled(
                        twoFactorEnabled
                )
                .totpSecret(
                        totpSecret
                )
                .build();
    }
}
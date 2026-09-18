package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.TwoFactorVerifyRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.TwoFactorChallenge;
import com.example.demo.entity.TwoFactorType;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.TwoFactorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TwoFactorService twoFactorService;
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthController controller;

    @Test
    void shouldReturnJwtWhenTwoFactorIsDisabled() {
        AuthRequest request = request();
        UserDetails details = details();
        Authentication authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        UserEntity user = user(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByUsername("angelo")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(details)).thenReturn("jwt-token");

        ResponseEntity<AuthResponse> result = controller.login(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getToken()).isEqualTo("jwt-token");
        assertThat(result.getBody().isTwoFactorRequired()).isFalse();
        verify(twoFactorService, never()).createChallenge(any());
    }

    @Test
    void shouldReturnChallengeWhenTwoFactorIsEnabled() {
        AuthRequest request = request();
        UserDetails details = details();
        Authentication authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        UserEntity user = user(true);
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = TwoFactorChallenge.builder()
                .id(challengeId)
                .user(user)
                .emailOtpHash("HASH")
                .expiresAt(Instant.now().plusSeconds(300))
                .attempts(0)
                .used(false)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByUsername("angelo")).thenReturn(Optional.of(user));
        when(twoFactorService.createChallenge(user)).thenReturn(challenge);

        ResponseEntity<AuthResponse> result = controller.login(request);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isTwoFactorRequired()).isTrue();
        assertThat(result.getBody().getChallengeId()).isEqualTo(challengeId);
        assertThat(result.getBody().getExpiresInSeconds()).isEqualTo(300L);
        assertThat(result.getBody().getToken()).isNull();
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldVerifyTwoFactorAndReturnJwt() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorVerifyRequest request = new TwoFactorVerifyRequest();
        request.setChallengeId(challengeId);
        UserEntity user = user(true);
        UserDetails details = details();

        when(twoFactorService.verify(challengeId, TwoFactorType.EMAIL_AUTH, "123456")).thenReturn(user);
        when(userDetailsService.loadUserByUsername("angelo")).thenReturn(details);
        when(jwtService.generateToken(details)).thenReturn("verified-token");

        ResponseEntity<AuthResponse> result = controller.verify2Fa(request, "123456", TwoFactorType.EMAIL_AUTH);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getToken()).isEqualTo("verified-token");
        assertThat(result.getBody().isTwoFactorRequired()).isFalse();
    }

    private AuthRequest request() {
        AuthRequest request = new AuthRequest();
        request.setUsername("angelo");
        request.setPassword("Mono96mh");
        return request;
    }

    private UserDetails details() {
        return User.withUsername("angelo")
                .password("encoded")
                .roles("USER")
                .build();
    }

    private UserEntity user(boolean twoFactor) {
        return UserEntity.builder()
                .id(1L)
                .username("angelo")
                .email("angelo@example.com")
                .password("encoded")
                .role(Role.USER)
                .enabled(true)
                .twoFactorEnabled(twoFactor)
                .totpSecret(twoFactor ? "SECRET" : null)
                .build();
    }
}

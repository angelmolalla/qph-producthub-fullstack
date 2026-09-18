package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.TwoFactorVerifyRequest;
import com.example.demo.entity.TwoFactorChallenge;
import com.example.demo.entity.TwoFactorType;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TwoFactorService twoFactorService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody
            AuthRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails)
                        authentication.getPrincipal();

        UserEntity user =
                userRepository
                        .findByUsername(
                                userDetails.getUsername()
                        )
                        .orElseThrow();

        if (!Boolean.TRUE.equals(
                user.getTwoFactorEnabled())) {

            String token =
                    jwtService.generateToken(
                            userDetails
                    );

            return ResponseEntity.ok(
                    AuthResponse.builder()
                            .token(token)
                            .twoFactorRequired(false)
                            .build()
            );
        }

        TwoFactorChallenge challenge =
                twoFactorService
                        .createChallenge(user);

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .twoFactorRequired(true)
                        .challengeId(
                                challenge.getId()
                        )
                        .expiresInSeconds(300L)
                        .build()
        );
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<AuthResponse> verify2Fa(
            @Valid
            @RequestBody
            TwoFactorVerifyRequest request,

            @RequestHeader("X-2FA-CODE")
            String code,

            @RequestHeader("X-2FA-TYPE")
            TwoFactorType type) {

        UserEntity user =
                twoFactorService.verify(
                        request.getChallengeId(),
                        type,
                        code
                );

        UserDetails userDetails =
                userDetailsService
                        .loadUserByUsername(
                                user.getUsername()
                        );

        String token =
                jwtService.generateToken(
                        userDetails
                );

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .token(token)
                        .twoFactorRequired(false)
                        .build()
        );
    }
}
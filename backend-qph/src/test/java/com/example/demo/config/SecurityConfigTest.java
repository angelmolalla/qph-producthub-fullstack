package com.example.demo.config;

import com.example.demo.security.JwtAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private SecurityConfig config;

    @BeforeEach
    void setUp() {
        config = new SecurityConfig(
                mock(JwtAuthFilter.class),
                mock(UserDetailsService.class)
        );
    }

    @Test
    void shouldCreateBcryptPasswordEncoder() {
        PasswordEncoder encoder = config.passwordEncoder();

        String encoded = encoder.encode("admin123");
        assertThat(encoded).isNotEqualTo("admin123");
        assertThat(encoder.matches("admin123", encoded)).isTrue();
    }

    @Test
    void shouldCreateAuthenticationProvider() {
        AuthenticationProvider provider = config.authenticationProvider();

        assertThat(provider).isNotNull();
    }

    @Test
    void shouldConfigureCorsForAngularFrontend() {
        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        CorsConfiguration cors = source.getCorsConfiguration(request);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("http://localhost:4200");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
        assertThat(cors.getAllowCredentials()).isTrue();
    }
}

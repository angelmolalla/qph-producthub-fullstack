package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.UserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;


class EmailTemplateServiceTest {

    private EmailTemplateServiceImpl service;


    @BeforeEach
    void setUp() {

        ClassLoaderTemplateResolver resolver =
                new ClassLoaderTemplateResolver();

        resolver.setPrefix(
                "templates/"
        );

        resolver.setSuffix(
                ".html"
        );

        resolver.setTemplateMode(
                TemplateMode.HTML
        );

        resolver.setCharacterEncoding(
                "UTF-8"
        );

        resolver.setCacheable(
                false
        );


        SpringTemplateEngine engine =
                new SpringTemplateEngine();

        engine.setTemplateResolver(
                resolver
        );


        service =
                new EmailTemplateServiceImpl(
                        engine
                );


        ReflectionTestUtils.setField(
                service,
                "frontendUrl",
                "http://localhost:4200"
        );

        ReflectionTestUtils.setField(
                service,
                "otpExpirationSeconds",
                300L
        );
    }


    @Test
    void shouldRenderAccountCreatedTemplate() {

        String html =
                service.accountCreated(
                        user()
                );


        assertThat(html)
                .contains(
                        "angelo"
                )
                .contains(
                        "http://localhost:4200"
                )
                .contains(
                        "Tu cuenta"
                );
    }


    @Test
    void shouldRenderOtpTemplate() {

        String html =
                service.emailOtp(
                        user(),
                        "123456"
                );


        assertThat(html)
                .contains(
                        "123456"
                )
                .contains(
                        "5"
                )
                .contains(
                        "Verifica tu identidad"
                );
    }


    @Test
    void shouldRenderAuthenticatorTemplate() {

        String html =
                service.authenticatorSetup(
                        user(),
                        "SECRET123"
                );


        assertThat(html)
                .contains(
                        "SECRET123"
                )
                .contains(
                        "cid:authenticatorQr"
                )
                .contains(
                        "Configura tu autenticador"
                );
    }


    private UserEntity user() {

        return UserEntity.builder()
                .id(1L)
                .username(
                        "angelo"
                )
                .email(
                        "angelo@example.com"
                )
                .password(
                        "encoded"
                )
                .role(
                        Role.USER
                )
                .enabled(
                        true
                )
                .twoFactorEnabled(
                        true
                )
                .totpSecret(
                        "SECRET123"
                )
                .build();
    }
}
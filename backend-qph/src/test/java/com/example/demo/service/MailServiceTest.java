package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.UserEntity;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTemplateService emailTemplateService;

    private MailServiceImpl service;

    @BeforeEach
    void setUp() {

        service =
                new MailServiceImpl(
                        mailSender,
                        emailTemplateService
                );

        ReflectionTestUtils.setField(
                service,
                "from",
                "noreply@example.com"
        );
    }

    @Test
    void shouldSendAccountCreatedEmail() {

        MimeMessage message =
                new MimeMessage(
                        Session.getInstance(
                                new Properties()
                        )
                );

        when(
                mailSender.createMimeMessage()
        )
                .thenReturn(message);

        when(
                emailTemplateService.accountCreated(
                        any(UserEntity.class)
                )
        )
                .thenReturn(
                        "<html>ok</html>"
                );

        boolean sent =
                service.sendAccountCreated(
                        user()
                );

        assertThat(sent)
                .isTrue();

        verify(mailSender)
                .send(message);
    }

    @Test
    void shouldNotPropagateSmtpFailure() {

        MimeMessage message =
                new MimeMessage(
                        Session.getInstance(
                                new Properties()
                        )
                );

        when(
                mailSender.createMimeMessage()
        )
                .thenReturn(message);

        when(
                emailTemplateService.accountCreated(
                        any(UserEntity.class)
                )
        )
                .thenReturn(
                        "<html>ok</html>"
                );

        doThrow(
                new MailSendException(
                        "SMTP unavailable"
                )
        )
                .when(mailSender)
                .send(message);

        boolean sent =
                service.sendAccountCreated(
                        user()
                );

        assertThat(sent)
                .isFalse();
    }

    private UserEntity user() {

        return UserEntity.builder()
                .id(1L)
                .username("angelo")
                .email("angelo@example.com")
                .password("encoded")
                .role(Role.USER)
                .enabled(true)
                .twoFactorEnabled(false)
                .build();
    }
}

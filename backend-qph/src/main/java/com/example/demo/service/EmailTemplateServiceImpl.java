package com.example.demo.service;

import com.example.demo.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Year;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final SpringTemplateEngine templateEngine;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.2fa.expiration-seconds:300}")
    private long otpExpirationSeconds;

    @Override
    public String accountCreated(
            UserEntity user) {

        Context context =
                baseContext(user);

        context.setVariable(
                "frontendUrl",
                frontendUrl
        );

        return templateEngine.process(
                "email/account-created",
                context
        );
    }

    @Override
    public String emailOtp(
            UserEntity user,
            String otp) {

        Context context =
                baseContext(user);

        context.setVariable(
                "otp",
                otp
        );

        context.setVariable(
                "expirationMinutes",
                Math.max(
                        1,
                        otpExpirationSeconds / 60
                )
        );

        return templateEngine.process(
                "email/email-otp",
                context
        );
    }

    @Override
    public String authenticatorSetup(
            UserEntity user,
            String secret) {

        Context context =
                baseContext(user);

        context.setVariable(
                "secret",
                secret
        );

        return templateEngine.process(
                "email/authenticator-setup",
                context
        );
    }

    private Context baseContext(
            UserEntity user) {

        Context context =
                new Context(
                        Locale.forLanguageTag("es")
                );

        context.setVariable(
                "username",
                user.getUsername()
        );

        context.setVariable(
                "email",
                user.getEmail()
        );

        context.setVariable(
                "currentYear",
                Year.now().getValue()
        );

        context.setVariable(
                "appName",
                "QPH ProductHub"
        );

        return context;
    }
}

package com.example.demo.service;

import com.example.demo.entity.UserEntity;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl
        implements MailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public boolean sendAccountCreated(
            UserEntity user) {

        try {
            String html =
                    emailTemplateService
                            .accountCreated(user);

            return sendHtml(
                    user.getEmail(),
                    "Bienvenido a QPH ProductHub",
                    html
            );

        } catch (Exception ex) {

            log.error(
                    "No se pudo preparar el correo de cuenta creada para {}",
                    maskEmail(user.getEmail()),
                    ex
            );

            return false;
        }
    }

    @Override
    public boolean sendEmailOtp(
            UserEntity user,
            String otp) {

        try {
            String html =
                    emailTemplateService
                            .emailOtp(
                                    user,
                                    otp
                            );

            return sendHtml(
                    user.getEmail(),
                    "Código de verificación - QPH ProductHub",
                    html
            );

        } catch (Exception ex) {

            log.error(
                    "No se pudo preparar el correo OTP para {}",
                    maskEmail(user.getEmail()),
                    ex
            );

            return false;
        }
    }

    @Override
    public boolean sendAuthenticatorSetup(
            UserEntity user,
            String secret,
            byte[] qrImage) {

        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(from);
            helper.setTo(user.getEmail());
            helper.setSubject(
                    "Configura tu autenticador - QPH ProductHub"
            );

            String html =
                    emailTemplateService
                            .authenticatorSetup(
                                    user,
                                    secret
                            );

            helper.setText(
                    html,
                    true
            );

            helper.addInline(
                    "authenticatorQr",
                    new ByteArrayResource(
                            qrImage
                    ),
                    "image/png"
            );

            mailSender.send(message);

            log.info(
                    "Correo de configuración TOTP enviado a {}",
                    maskEmail(user.getEmail())
            );

            return true;

        } catch (Exception ex) {

            log.error(
                    "No se pudo enviar el correo de configuración TOTP a {}",
                    maskEmail(user.getEmail()),
                    ex
            );

            return false;
        }
    }

    private boolean sendHtml(
            String to,
            String subject,
            String html) {

        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            false,
                            "UTF-8"
                    );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(
                    html,
                    true
            );

            mailSender.send(message);

            log.info(
                    "Correo '{}' enviado a {}",
                    subject,
                    maskEmail(to)
            );

            return true;

        } catch (Exception ex) {

            log.error(
                    "No se pudo enviar el correo '{}' a {}",
                    subject,
                    maskEmail(to),
                    ex
            );

            return false;
        }
    }

    private String maskEmail(
            String email) {

        if (email == null
                || !email.contains("@")) {
            return "***";
        }

        int at =
                email.indexOf('@');

        String local =
                email.substring(
                        0,
                        at
                );

        String domain =
                email.substring(at);

        if (local.length() <= 2) {
            return "**" + domain;
        }

        return local.substring(0, 2)
                + "***"
                + domain;
    }
}

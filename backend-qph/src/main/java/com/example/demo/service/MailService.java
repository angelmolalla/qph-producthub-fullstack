package com.example.demo.service;

import com.example.demo.entity.UserEntity;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendAccountCreated(
            UserEntity user) {

        sendHtml(
                user.getEmail(),
                "Cuenta creada - QPH",
                """
                <h2>Tu cuenta ha sido creada</h2>

                <p>Hola %s,</p>

                <p>Tu cuenta fue creada correctamente.</p>

                <p>
                    <strong>Usuario:</strong> %s
                </p>

                <p>
                    Ya puedes iniciar sesión en la aplicación.
                </p>
                """.formatted(
                        user.getUsername(),
                        user.getUsername()
                )
        );
    }

    public void sendEmailOtp(
            UserEntity user,
            String otp) {

        sendHtml(
                user.getEmail(),
                "Código de verificación - QPH",
                """
                <h2>Código de verificación</h2>

                <p>Hola %s,</p>

                <p>Tu código OTP es:</p>

                <h1>%s</h1>

                <p>
                    Este código expira en 5 minutos.
                </p>

                <p>
                    Si no intentaste iniciar sesión,
                    ignora este mensaje.
                </p>
                """.formatted(
                        user.getUsername(),
                        otp
                )
        );
    }

    public void sendAuthenticatorSetup(
            UserEntity user,
            String secret,
            String otpAuthUri,
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
                    "Configura Microsoft Authenticator - QPH"
            );

            helper.setText(
                    """
                    <h2>Configura tu autenticador</h2>

                    <p>Hola %s,</p>

                    <p>
                        La autenticación de dos factores
                        está habilitada para tu cuenta.
                    </p>

                    <p>
                        Abre Microsoft Authenticator y
                        escanea el código QR adjunto.
                    </p>

                    <p>
                        Si no puedes escanearlo,
                        utiliza esta clave manual:
                    </p>

                    <p>
                        <strong>%s</strong>
                    </p>

                    <p>
                        Al iniciar sesión podrás utilizar
                        tanto el código enviado por correo
                        como el código generado por
                        Microsoft Authenticator.
                    </p>
                    """.formatted(
                            user.getUsername(),
                            secret
                    ),
                    true
            );

            helper.addAttachment(
                    "qph-authenticator-qr.png",
                    new ByteArrayResource(qrImage),
                    "image/png"
            );

            mailSender.send(message);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo enviar configuración TOTP",
                    e
            );
        }
    }

    private void sendHtml(
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
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo enviar correo a " + to,
                    e
            );
        }
    }
}
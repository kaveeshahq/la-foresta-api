package com.laforesta.api.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleSmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Override
    public void sendEmailVerification(
            String to,
            String fullName,
            String verificationToken
    ) {

        String verificationUrl =
                frontendUrl
                        + "/verify-email?token="
                        + verificationToken;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);

        message.setSubject(
                "Verify your La Foresta account"
        );

        message.setText(
                """
                Hi %s,

                Welcome to La Foresta.

                Please verify your email address using the link below:

                %s

                This verification link expires in 24 hours.

                If you did not create this account, you can ignore this email.

                La Foresta
                """.formatted(
                        fullName,
                        verificationUrl
                )
        );

        mailSender.send(message);
    }

    @Override
    public void sendPasswordReset(
            String to,
            String fullName,
            String resetToken
    ) {

        String resetUrl =
                frontendUrl
                        + "/reset-password?token="
                        + resetToken;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);

        message.setSubject(
                "Reset your La Foresta password"
        );

        message.setText(
                """
                Hi %s,

                We received a request to reset your La Foresta password.

                Use the link below to choose a new password:

                %s

                This link expires in 30 minutes.

                If you did not request a password reset, you can ignore this email.

                La Foresta
                """.formatted(
                        fullName,
                        resetUrl
                )
        );

        mailSender.send(message);
    }
}
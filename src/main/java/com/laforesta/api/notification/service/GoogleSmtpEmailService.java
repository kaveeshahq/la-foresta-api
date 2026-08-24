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
}
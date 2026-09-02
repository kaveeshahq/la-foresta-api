package com.laforesta.api.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GoogleSmtpEmailService
        implements EmailService {

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
                UriComponentsBuilder
                        .fromUriString(frontendUrl)
                        .path("/verify-email")
                        .queryParam(
                                "token",
                                verificationToken
                        )
                        .build()
                        .encode()
                        .toUriString();

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
                UriComponentsBuilder
                        .fromUriString(frontendUrl)
                        .path("/reset-password")
                        .queryParam(
                                "token",
                                resetToken
                        )
                        .build()
                        .encode()
                        .toUriString();

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

    @Override
    public void sendGuestTicketConfirmation(
            String to,
            String fullName,
            String accessToken
    ) {

        String ticketUrl =
                UriComponentsBuilder
                        .fromUriString(frontendUrl)
                        .path("/tickets/guest")
                        .queryParam(
                                "token",
                                accessToken
                        )
                        .build()
                        .encode()
                        .toUriString();

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);

        message.setSubject(
                "Your La Foresta tickets are ready"
        );

        message.setText(
                """
                Hi %s,

                Your payment was successful and your La Foresta tickets are ready.

                You can access your tickets using the secure link below:

                %s

                Keep this link private. Anyone who has this link may be able to access your tickets.

                We look forward to seeing you at La Foresta.

                La Foresta
                """.formatted(
                        fullName,
                        ticketUrl
                )
        );

        mailSender.send(message);
    }
}
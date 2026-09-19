package com.laforesta.api.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleSmtpEmailService
        implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.retry.max-attempts:3}")
    private int configuredMaxAttempts;

    @Value("${app.mail.retry.initial-delay-ms:1000}")
    private long configuredInitialDelayMillis;

    @Override
    public void sendEmailVerification(
            String to,
            String fullName,
            String verificationToken
    ) {

        String verificationUrl =
                createFrontendUrl(
                        "/verify-email",
                        verificationToken
                );

        String name = normalizeName(fullName);

        String plainText =
                """
                Hi %s,

                Welcome to La Foresta.

                Verify your email address using the secure link below:

                %s

                This verification link expires in 24 hours.

                If you did not create this account, you can ignore this email.

                La Foresta
                """.formatted(
                        name,
                        verificationUrl
                );

        String html =
                buildHtmlEmail(
                        "Confirm your signal",
                        "ACCOUNT VERIFICATION",
                        "Welcome to<br>La Foresta.",
                        "Hi " + name + ",",
                        "Verify your email address to activate your account and keep your tickets and purchases together.",
                        "Verify email address",
                        verificationUrl,
                        "This secure link expires in 24 hours. If you did not create this account, you can safely ignore this message."
                );

        sendHtmlEmail(
                "account verification",
                to,
                "Verify your La Foresta account",
                plainText,
                html
        );
    }

    @Override
    public void sendPasswordReset(
            String to,
            String fullName,
            String resetToken
    ) {

        String resetUrl =
                createFrontendUrl(
                        "/reset-password",
                        resetToken
                );

        String name = normalizeName(fullName);

        String plainText =
                """
                Hi %s,

                We received a request to reset your La Foresta password.

                Use the secure link below to choose a new password:

                %s

                This link expires in 30 minutes.

                If you did not request a password reset, you can ignore this email.

                La Foresta
                """.formatted(
                        name,
                        resetUrl
                );

        String html =
                buildHtmlEmail(
                        "Restore your access",
                        "PASSWORD RESET",
                        "Create new<br>access.",
                        "Hi " + name + ",",
                        "A password reset was requested for your La Foresta account. Use the secure link below to choose a new password.",
                        "Reset password",
                        resetUrl,
                        "This secure link expires in 30 minutes. If you did not request a reset, you can safely ignore this message."
                );

        sendHtmlEmail(
                "password reset",
                to,
                "Reset your La Foresta password",
                plainText,
                html
        );
    }

    @Override
    public void sendGuestTicketConfirmation(
            String to,
            String fullName,
            String accessToken
    ) {

        String ticketUrl =
                createFrontendUrl(
                        "/tickets/guest",
                        accessToken
                );

        String name = normalizeName(fullName);

        String plainText =
                """
                Hi %s,

                Your payment was successful and your La Foresta tickets are ready.

                Access your tickets using the secure link below:

                %s

                Keep this link private. Anyone with this link may be able to access your tickets.

                We look forward to seeing you at La Foresta.

                La Foresta
                """.formatted(
                        name,
                        ticketUrl
                );

        String html =
                buildHtmlEmail(
                        "Your entry is secured",
                        "TICKETS READY",
                        "See you beneath<br>the eclipse.",
                        "Hi " + name + ",",
                        "Your payment was successful and your La Foresta tickets are ready. Open your secure ticket wallet before arriving at the venue.",
                        "Open my tickets",
                        ticketUrl,
                        "Keep this link private. Anyone with access to this link may be able to view your tickets."
                );

        sendHtmlEmail(
                "guest ticket confirmation",
                to,
                "Your La Foresta tickets are ready",
                plainText,
                html
        );
    }

    private String createFrontendUrl(
            String path,
            String token
    ) {

        String normalizedFrontendUrl =
                frontendUrl.replaceFirst(
                        "/+$",
                        ""
                );

        return UriComponentsBuilder
                .fromUriString(normalizedFrontendUrl)
                .path(path)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
    }

    private void sendHtmlEmail(
            String deliveryType,
            String to,
            String subject,
            String plainText,
            String html
    ) {

        int maxAttempts =
                Math.max(
                        configuredMaxAttempts,
                        1
                );

        MailSendException lastFailure = null;

        for (
                int attempt = 1;
                attempt <= maxAttempts;
                attempt++
        ) {

            try {

                MimeMessage message =
                        mailSender.createMimeMessage();

                MimeMessageHelper helper =
                        new MimeMessageHelper(
                                message,
                                false,
                                StandardCharsets.UTF_8.name()
                        );

                helper.setFrom(fromAddress);
                helper.setTo(to);
                helper.setSubject(subject);

                /*
                 * The first value is the plain-text fallback.
                 * The second value is the HTML version.
                 */
                helper.setText(
                        plainText,
                        html
                );

                mailSender.send(message);

                if (attempt > 1) {
                    log.info(
                            "{} email delivered on attempt {}",
                            deliveryType,
                            attempt
                    );
                }

                return;

            } catch (
                    MessagingException
                    | MailException exception
            ) {

                lastFailure =
                        new MailSendException(
                                "Unable to deliver "
                                        + deliveryType
                                        + " email",
                                exception
                        );

                log.warn(
                        "{} email delivery attempt {}/{} failed",
                        deliveryType,
                        attempt,
                        maxAttempts
                );

                if (attempt < maxAttempts) {
                    pauseBeforeRetry(attempt);
                }
            }
        }

        throw lastFailure != null
                ? lastFailure
                : new MailSendException(
                "Unable to deliver "
                        + deliveryType
                        + " email"
        );
    }

    private void pauseBeforeRetry(
            int failedAttempt
    ) {

        long baseDelay =
                Math.max(
                        configuredInitialDelayMillis,
                        0L
                );

        long delayMillis =
                Math.min(
                        baseDelay * failedAttempt,
                        10_000L
                );

        try {

            Thread.sleep(delayMillis);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new MailSendException(
                    "Email delivery retry was interrupted",
                    exception
            );
        }
    }

    private String normalizeName(
            String fullName
    ) {

        if (fullName == null
                || fullName.isBlank()) {

            return "there";
        }

        return fullName.trim();
    }

    private String buildHtmlEmail(
            String preheader,
            String eyebrow,
            String heading,
            String greeting,
            String body,
            String buttonLabel,
            String buttonUrl,
            String note
    ) {

        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>La Foresta</title>
                </head>
                <body style="margin:0;padding:0;background:#050706;color:#f3f5ef;font-family:Arial,Helvetica,sans-serif;">
                    <div style="display:none;max-height:0;overflow:hidden;opacity:0;">
                        %s
                    </div>

                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#050706;">
                        <tr>
                            <td align="center" style="padding:32px 16px;">
                                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" style="width:100%%;max-width:600px;border:1px solid #252a26;background:#090c0a;">
                                    <tr>
                                        <td style="padding:28px 32px;border-bottom:1px solid #252a26;">
                                            <div style="font-size:18px;font-weight:700;letter-spacing:-0.5px;color:#f3f5ef;">
                                                LA FORESTA
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:48px 32px 24px;">
                                            <div style="font-size:10px;letter-spacing:2.4px;color:#55df6c;">
                                                %s
                                            </div>

                                            <h1 style="margin:20px 0 0;font-size:46px;line-height:0.98;letter-spacing:-2.5px;font-weight:500;color:#f3f5ef;">
                                                %s
                                            </h1>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:8px 32px 40px;">
                                            <p style="margin:0 0 16px;font-size:16px;line-height:1.7;color:#f3f5ef;">
                                                %s
                                            </p>

                                            <p style="margin:0;font-size:15px;line-height:1.8;color:#a7afa8;">
                                                %s
                                            </p>

                                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin:32px 0;">
                                                <tr>
                                                    <td style="background:#55df6c;">
                                                        <a href="%s" style="display:inline-block;padding:16px 24px;color:#050706;text-decoration:none;font-size:11px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;">
                                                            %s
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>

                                            <p style="margin:0;font-size:12px;line-height:1.7;color:#727a73;">
                                                %s
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:24px 32px;border-top:1px solid #252a26;">
                                            <p style="margin:0;font-size:10px;line-height:1.6;letter-spacing:1.5px;color:#727a73;">
                                                LA FORESTA / SRI LANKA
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(preheader),
                escapeHtml(eyebrow),
                heading,
                escapeHtml(greeting),
                escapeHtml(body),
                escapeHtml(buttonUrl),
                escapeHtml(buttonLabel),
                escapeHtml(note)
        );
    }

    private String escapeHtml(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
package com.laforesta.api.notification.listener;

import com.laforesta.api.notification.event.EmailVerificationRequestedEvent;
import com.laforesta.api.notification.event.PasswordResetRequestedEvent;
import com.laforesta.api.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEmailListener {

    private final EmailService emailService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleEmailVerification(
            EmailVerificationRequestedEvent event
    ) {

        try {

            emailService.sendEmailVerification(
                    event.email(),
                    event.fullName(),
                    event.verificationToken()
            );

            log.info(
                    "Account verification email sent"
            );

        } catch (Exception exception) {

            /*
             * Account creation or token generation has already committed.
             * An SMTP failure must not roll back a valid account.
             */
            log.error(
                    "Failed to send account verification email",
                    exception
            );
        }
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handlePasswordReset(
            PasswordResetRequestedEvent event
    ) {

        try {

            emailService.sendPasswordReset(
                    event.email(),
                    event.fullName(),
                    event.resetToken()
            );

            log.info(
                    "Password reset email sent"
            );

        } catch (Exception exception) {

            /*
             * Do not reveal whether an email address belongs
             * to an account through the API response.
             */
            log.error(
                    "Failed to send password reset email",
                    exception
            );
        }
    }
}
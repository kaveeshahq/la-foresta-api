package com.laforesta.api.notification.listener;

import com.laforesta.api.notification.event.GuestTicketEmailEvent;
import com.laforesta.api.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class GuestTicketEmailListener {

    private final EmailService emailService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleGuestTicketEmail(
            GuestTicketEmailEvent event
    ) {

        try {

            emailService.sendGuestTicketConfirmation(
                    event.email(),
                    event.fullName(),
                    event.accessToken()
            );

            log.info(
                    "Guest ticket email sent for order {}",
                    event.orderId()
            );

        } catch (Exception exception) {

            /*
             * Important:
             * payment/ticket transaction is already committed.
             * Email failure must not undo a successful purchase.
             */
            log.error(
                    "Failed to send guest ticket email for order {}",
                    event.orderId(),
                    exception
            );
        }
    }
}
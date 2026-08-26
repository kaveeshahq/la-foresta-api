package com.laforesta.api.ticket.service;

import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.payment.entity.PaymentTransaction;
import com.laforesta.api.payment.model.PaymentStatus;
import com.laforesta.api.payment.repository.PaymentTransactionRepository;
import com.laforesta.api.ticket.entity.TicketReservation;
import com.laforesta.api.ticket.model.ReservationStatus;
import com.laforesta.api.ticket.repository.TicketReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationExpiryService {

    private final TicketReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireReservations() {

        OffsetDateTime now = OffsetDateTime.now();

        List<TicketReservation> expiredReservations =
                reservationRepository
                        .findAllByStatusAndExpiresAtBefore(
                                ReservationStatus.ACTIVE,
                                now
                        );

        for (TicketReservation reservation : expiredReservations) {

            reservation.setStatus(
                    ReservationStatus.EXPIRED
            );

            orderRepository
                    .findByReservation(reservation)
                    .ifPresent(order -> {

                        if (order.getStatus()
                                == OrderStatus.PENDING_PAYMENT) {

                            order.setStatus(
                                    OrderStatus.CANCELLED
                            );

                            List<PaymentTransaction> pendingPayments =
                                    paymentTransactionRepository
                                            .findAllByOrderIdAndStatus(
                                                    order.getId(),
                                                    PaymentStatus.PENDING
                                            );

                            for (PaymentTransaction payment
                                    : pendingPayments) {

                                payment.setStatus(
                                        PaymentStatus.CANCELLED
                                );
                            }
                        }
                    });
        }
    }
}
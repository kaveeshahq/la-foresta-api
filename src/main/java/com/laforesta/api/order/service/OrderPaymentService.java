package com.laforesta.api.order.service;

import com.laforesta.api.notification.event.GuestTicketEmailEvent;
import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.ticket.entity.TicketReservation;
import com.laforesta.api.ticket.model.ReservationStatus;
import com.laforesta.api.ticket.service.TicketIssuanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final TicketIssuanceService ticketIssuanceService;
    private final GuestAccessTokenService guestAccessTokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void confirmPayment(
            UUID orderId
    ) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );

        /*
         * Idempotency.
         */
        if (order.getStatus()
                == OrderStatus.PAID) {

            ticketIssuanceService
                    .issueTicketsForOrder(order);

            return;
        }

        if (order.getStatus()
                != OrderStatus.PENDING_PAYMENT) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order is not pending payment"
            );
        }

        TicketReservation reservation =
                order.getReservation();

        if (reservation.getStatus()
                != ReservationStatus.ACTIVE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation is not active"
            );
        }

        if (!reservation.getExpiresAt()
                .isAfter(OffsetDateTime.now())) {

            reservation.setStatus(
                    ReservationStatus.EXPIRED
            );

            order.setStatus(
                    OrderStatus.CANCELLED
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation has expired"
            );
        }

        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        order.setStatus(
                OrderStatus.PAID
        );

        ticketIssuanceService
                .issueTicketsForOrder(order);

        /*
         * Guest ticket email.
         *
         * Generate this only during the first successful
         * payment confirmation.
         */
        if (order.getUser() == null
                && order.getGuestEmail() != null
                && !order.getGuestEmail().isBlank()
                && order.getGuestEmailAccessTokenHash() == null) {

            String emailAccessToken =
                    guestAccessTokenService
                            .generateToken();

            String tokenHash =
                    guestAccessTokenService
                            .hashToken(
                                    emailAccessToken
                            );

            order.setGuestEmailAccessTokenHash(
                    tokenHash
            );

            eventPublisher.publishEvent(
                    new GuestTicketEmailEvent(
                            order.getId(),
                            order.getGuestEmail(),
                            order.getGuestName(),
                            emailAccessToken
                    )
            );
        }
    }

    @Transactional
    public void failPayment(
            UUID orderId
    ) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );

        if (order.getStatus()
                != OrderStatus.PENDING_PAYMENT) {

            return;
        }

        order.setStatus(
                OrderStatus.PAYMENT_FAILED
        );

        TicketReservation reservation =
                order.getReservation();

        if (reservation.getStatus()
                == ReservationStatus.ACTIVE) {

            reservation.setStatus(
                    ReservationStatus.CANCELLED
            );
        }
    }
}
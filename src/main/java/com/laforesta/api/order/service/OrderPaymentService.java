package com.laforesta.api.order.service;

import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.ticket.entity.TicketReservation;
import com.laforesta.api.ticket.model.ReservationStatus;
import com.laforesta.api.ticket.service.TicketIssuanceService;
import lombok.RequiredArgsConstructor;
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
         * Idempotency:
         * If this order is already paid, make sure its tickets
         * have been issued, then safely return.
         */
        if (order.getStatus() == OrderStatus.PAID) {

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

        /*
         * If the order is no longer waiting for payment,
         * do nothing.
         */
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
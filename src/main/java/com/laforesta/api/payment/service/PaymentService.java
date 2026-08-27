package com.laforesta.api.payment.service;

import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.order.service.OrderPaymentService;
import com.laforesta.api.payment.dto.PaymentResponse;
import com.laforesta.api.payment.entity.PaymentTransaction;
import com.laforesta.api.payment.model.PaymentProvider;
import com.laforesta.api.payment.model.PaymentStatus;
import com.laforesta.api.payment.repository.PaymentTransactionRepository;
import com.laforesta.api.ticket.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderPaymentService orderPaymentService;

    /*
     * Registered-user payment initiation
     */
    @Transactional
    public PaymentResponse initiateMockPayment(
            UUID userId,
            UUID orderId
    ) {

        Order order = orderRepository
                .findByIdAndUserId(
                        orderId,
                        userId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );

        return initiatePaymentInternal(order);
    }

    /*
     * Guest payment initiation
     */
    @Transactional
    public PaymentResponse initiateGuestMockPayment(
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

        if (order.getUser() != null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Order is not a guest order"
            );
        }

        if (order.getGuestEmail() == null
                || order.getGuestEmail().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest order does not contain customer details"
            );
        }

        return initiatePaymentInternal(order);
    }

    /*
     * Shared payment-initiation logic
     */
    private PaymentResponse initiatePaymentInternal(
            Order order
    ) {

        if (order.getStatus()
                != OrderStatus.PENDING_PAYMENT) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order is not pending payment"
            );
        }

        if (order.getReservation().getStatus()
                != ReservationStatus.ACTIVE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation is not active"
            );
        }

        if (!order.getReservation()
                .getExpiresAt()
                .isAfter(OffsetDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation has expired"
            );
        }

        paymentRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
                        order.getId(),
                        PaymentStatus.PENDING
                )
                .ifPresent(payment -> {

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A pending payment already exists for this order"
                    );
                });

        PaymentTransaction payment =
                new PaymentTransaction();

        payment.setOrder(order);

        payment.setProvider(
                PaymentProvider.MOCK
        );

        payment.setStatus(
                PaymentStatus.PENDING
        );

        payment.setAmount(
                order.getTotalAmount()
        );

        payment.setCurrency(
                order.getCurrency()
        );

        PaymentTransaction saved =
                paymentRepository.save(
                        payment
                );

        return toResponse(saved);
    }

    @Transactional
    public PaymentResponse completeMockPayment(
            UUID paymentId
    ) {

        PaymentTransaction payment =
                getPayment(paymentId);

        if (payment.getStatus()
                == PaymentStatus.SUCCESS) {

            return toResponse(payment);
        }

        if (payment.getStatus()
                != PaymentStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment is not pending"
            );
        }

        Order order =
                payment.getOrder();

        if (!order.getReservation()
                .getExpiresAt()
                .isAfter(OffsetDateTime.now())) {

            payment.setStatus(
                    PaymentStatus.FAILED
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation has expired"
            );
        }

        orderPaymentService
                .confirmPayment(
                        order.getId()
                );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        payment.setProviderReference(
                "MOCK-" + UUID.randomUUID()
        );

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse failMockPayment(
            UUID paymentId
    ) {

        PaymentTransaction payment =
                getPayment(paymentId);

        if (payment.getStatus()
                == PaymentStatus.FAILED) {

            return toResponse(payment);
        }

        if (payment.getStatus()
                != PaymentStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment is not pending"
            );
        }

        orderPaymentService
                .failPayment(
                        payment
                                .getOrder()
                                .getId()
                );

        payment.setStatus(
                PaymentStatus.FAILED
        );

        payment.setProviderReference(
                "MOCK-FAILED-" + UUID.randomUUID()
        );

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentForUser(
            UUID userId,
            UUID paymentId
    ) {

        PaymentTransaction payment =
                getPayment(paymentId);

        Order order =
                payment.getOrder();

        if (order.getUser() == null
                || !order.getUser()
                .getId()
                .equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Payment not found"
            );
        }

        return toResponse(payment);
    }

    /*
     * Guest payment lookup
     */
    @Transactional(readOnly = true)
    public PaymentResponse getGuestPayment(
            UUID paymentId
    ) {

        PaymentTransaction payment =
                getPayment(paymentId);

        Order order =
                payment.getOrder();

        if (order.getUser() != null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Payment not found"
            );
        }

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForOrder(
            UUID userId,
            UUID orderId
    ) {

        Order order = orderRepository
                .findByIdAndUserId(
                        orderId,
                        userId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );

        return paymentRepository
                .findAllByOrderIdOrderByCreatedAtDesc(
                        order.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentTransaction getPayment(
            UUID paymentId
    ) {

        return paymentRepository
                .findById(paymentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Payment not found"
                        )
                );
    }

    private PaymentResponse toResponse(
            PaymentTransaction payment
    ) {

        return new PaymentResponse(
                payment.getId(),
                payment.getOrder()
                        .getId(),
                payment.getProvider(),
                payment.getStatus(),
                payment.getProviderReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
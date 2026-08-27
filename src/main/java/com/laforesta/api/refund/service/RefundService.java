package com.laforesta.api.refund.service;

import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.payment.entity.PaymentTransaction;
import com.laforesta.api.payment.model.PaymentStatus;
import com.laforesta.api.payment.repository.PaymentTransactionRepository;
import com.laforesta.api.refund.dto.RefundResponse;
import com.laforesta.api.refund.entity.RefundTransaction;
import com.laforesta.api.refund.model.RefundProvider;
import com.laforesta.api.refund.model.RefundStatus;
import com.laforesta.api.refund.repository.RefundTransactionRepository;
import com.laforesta.api.ticket.entity.Ticket;
import com.laforesta.api.ticket.model.TicketStatus;
import com.laforesta.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public RefundResponse refundOrder(
            UUID orderId,
            String reason
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
         * Idempotent recovery:
         * if the order is already refunded, return
         * the existing successful refund.
         */
        if (order.getStatus() == OrderStatus.REFUNDED) {

            RefundTransaction existingRefund =
                    refundTransactionRepository
                            .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
                                    orderId,
                                    RefundStatus.SUCCESS
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.CONFLICT,
                                            "Order is refunded but refund transaction is missing"
                                    )
                            );

            return toResponse(existingRefund);
        }

        if (order.getStatus() != OrderStatus.PAID) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only paid orders can be refunded"
            );
        }

        PaymentTransaction payment =
                paymentTransactionRepository
                        .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
                                orderId,
                                PaymentStatus.SUCCESS
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Successful payment transaction not found"
                                )
                        );

        RefundTransaction refund =
                new RefundTransaction();

        refund.setOrder(order);
        refund.setPaymentTransaction(payment);
        refund.setProvider(
                RefundProvider.MOCK
        );
        refund.setStatus(
                RefundStatus.SUCCESS
        );
        refund.setAmount(
                payment.getAmount()
        );
        refund.setCurrency(
                payment.getCurrency()
        );
        refund.setReason(
                reason
        );
        refund.setProviderReference(
                "MOCK-REFUND-" + UUID.randomUUID()
        );

        /*
         * Mark the order refunded.
         */
        order.setStatus(
                OrderStatus.REFUNDED
        );

        /*
         * Invalidate all tickets from this order.
         */
        List<Ticket> tickets =
                ticketRepository
                        .findAllByOrderId(
                                orderId
                        );

        for (Ticket ticket : tickets) {

            ticket.setStatus(
                    TicketStatus.REFUNDED
            );
        }

        RefundTransaction savedRefund =
                refundTransactionRepository
                        .save(refund);

        return toResponse(savedRefund);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsForOrder(
            UUID orderId
    ) {

        if (!orderRepository.existsById(orderId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order not found"
            );
        }

        return refundTransactionRepository
                .findAllByOrderIdOrderByCreatedAtDesc(
                        orderId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RefundResponse toResponse(
            RefundTransaction refund
    ) {

        return new RefundResponse(
                refund.getId(),
                refund.getOrder().getId(),

                refund.getPaymentTransaction() != null
                        ? refund.getPaymentTransaction().getId()
                        : null,

                refund.getProvider(),
                refund.getStatus(),

                refund.getAmount(),
                refund.getCurrency(),

                refund.getReason(),
                refund.getProviderReference(),

                refund.getCreatedAt(),
                refund.getUpdatedAt()
        );
    }
}
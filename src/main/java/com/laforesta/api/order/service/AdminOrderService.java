package com.laforesta.api.order.service;

import com.laforesta.api.order.dto.*;
import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.payment.repository.PaymentTransactionRepository;
import com.laforesta.api.refund.repository.RefundTransactionRepository;
import com.laforesta.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public AdminOrderResponse getOrder(
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

        var items = order.getItems()
                .stream()
                .map(item ->
                        new OrderItemResponse(
                                item.getTicketType().getId(),
                                item.getTicketTypeName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getCurrency(),
                                item.getLineTotal()
                        )
                )
                .toList();

        var payments =
                paymentTransactionRepository
                        .findAllByOrderIdOrderByCreatedAtDesc(
                                orderId
                        )
                        .stream()
                        .map(payment ->
                                new AdminPaymentResponse(
                                        payment.getId(),
                                        payment.getProvider(),
                                        payment.getStatus(),
                                        payment.getProviderReference(),
                                        payment.getAmount(),
                                        payment.getCurrency(),
                                        payment.getCreatedAt()
                                )
                        )
                        .toList();

        var refunds =
                refundTransactionRepository
                        .findAllByOrderIdOrderByCreatedAtDesc(
                                orderId
                        )
                        .stream()
                        .map(refund ->
                                new AdminRefundResponse(
                                        refund.getId(),
                                        refund.getProvider(),
                                        refund.getStatus(),
                                        refund.getAmount(),
                                        refund.getCurrency(),
                                        refund.getReason(),
                                        refund.getProviderReference(),
                                        refund.getCreatedAt()
                                )
                        )
                        .toList();

        var tickets =
                ticketRepository
                        .findAllByOrderId(orderId)
                        .stream()
                        .map(ticket ->
                                new AdminTicketResponse(
                                        ticket.getId(),
                                        ticket.getTicketNumber(),
                                        ticket.getStatus(),
                                        ticket.getTicketType().getId(),
                                        ticket.getTicketType().getName()
                                )
                        )
                        .toList();

        return new AdminOrderResponse(
                order.getId(),

                order.getUser().getId(),
                order.getUser().getEmail(),

                order.getReservation().getId(),

                order.getStatus(),

                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getTotalAmount(),

                order.getPromoCodeSnapshot(),
                order.getCurrency(),

                items,
                payments,
                refunds,
                tickets,

                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
package com.laforesta.api.order.dto;

import com.laforesta.api.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminOrderResponse(

        UUID orderId,

        UUID userId,
        String customerEmail,

        UUID reservationId,

        OrderStatus status,

        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,

        String promoCode,
        String currency,

        List<OrderItemResponse> items,
        List<AdminPaymentResponse> payments,
        List<AdminRefundResponse> refunds,
        List<AdminTicketResponse> tickets,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
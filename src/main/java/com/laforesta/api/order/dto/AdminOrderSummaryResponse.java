package com.laforesta.api.order.dto;

import com.laforesta.api.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminOrderSummaryResponse(

        UUID orderId,
        UUID userId,
        String customerEmail,
        String customerName,

        OrderStatus status,

        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,

        String promoCode,
        String currency,

        OffsetDateTime createdAt

) {
}
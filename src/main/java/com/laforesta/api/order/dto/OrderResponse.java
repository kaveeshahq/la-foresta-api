package com.laforesta.api.order.dto;

import com.laforesta.api.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(

        UUID orderId,
        UUID reservationId,
        OrderStatus status,

        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,

        String promoCode,

        String currency,
        List<OrderItemResponse> items,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
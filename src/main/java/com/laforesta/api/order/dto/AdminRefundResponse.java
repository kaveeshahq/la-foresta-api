package com.laforesta.api.order.dto;

import com.laforesta.api.refund.model.RefundProvider;
import com.laforesta.api.refund.model.RefundStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminRefundResponse(

        UUID refundId,
        RefundProvider provider,
        RefundStatus status,
        BigDecimal amount,
        String currency,
        String reason,
        String providerReference,
        OffsetDateTime createdAt

) {
}
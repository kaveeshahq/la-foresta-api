package com.laforesta.api.refund.dto;

import com.laforesta.api.refund.model.RefundProvider;
import com.laforesta.api.refund.model.RefundStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RefundResponse(

        UUID refundId,
        UUID orderId,
        UUID paymentTransactionId,

        RefundProvider provider,
        RefundStatus status,

        BigDecimal amount,
        String currency,

        String reason,
        String providerReference,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
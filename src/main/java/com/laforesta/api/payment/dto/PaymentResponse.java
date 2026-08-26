package com.laforesta.api.payment.dto;

import com.laforesta.api.payment.model.PaymentProvider;
import com.laforesta.api.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(

        UUID paymentId,
        UUID orderId,
        PaymentProvider provider,
        PaymentStatus status,
        String providerReference,
        BigDecimal amount,
        String currency,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
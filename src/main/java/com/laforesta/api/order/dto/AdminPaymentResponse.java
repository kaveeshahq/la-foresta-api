package com.laforesta.api.order.dto;

import com.laforesta.api.payment.model.PaymentProvider;
import com.laforesta.api.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminPaymentResponse(

        UUID paymentId,
        PaymentProvider provider,
        PaymentStatus status,
        String providerReference,
        BigDecimal amount,
        String currency,
        OffsetDateTime createdAt

) {
}
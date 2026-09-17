package com.laforesta.api.promo.dto;

import com.laforesta.api.promo.model.DiscountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminPromoCodeResponse(

        UUID id,

        UUID eventId,
        String eventTitle,

        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,

        Integer usageLimit,
        Integer perUserLimit,

        long redemptionCount,
        Long remainingUses,

        OffsetDateTime validFrom,
        OffsetDateTime validUntil,

        boolean active,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
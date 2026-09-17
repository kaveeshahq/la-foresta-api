package com.laforesta.api.promo.dto;

import com.laforesta.api.promo.model.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PromoCodeUpsertRequest(

        UUID eventId,

        @NotBlank(message = "Promo code is required")
        @Size(max = 50)
        String code,

        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(
                value = "0.01",
                message = "Discount value must be greater than zero"
        )
        BigDecimal discountValue,

        @DecimalMin(
                value = "0.00",
                message = "Minimum order amount cannot be negative"
        )
        BigDecimal minimumOrderAmount,

        @Min(
                value = 1,
                message = "Usage limit must be greater than zero"
        )
        Integer usageLimit,

        @Min(
                value = 1,
                message = "Per-user limit must be greater than zero"
        )
        Integer perUserLimit,

        OffsetDateTime validFrom,

        OffsetDateTime validUntil,

        boolean active

) {
}
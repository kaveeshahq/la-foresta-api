package com.laforesta.api.promo.dto;

import com.laforesta.api.promo.model.DiscountType;

import java.math.BigDecimal;
import java.util.UUID;

public record PromoCalculationResult(

        UUID promoCodeId,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,

        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount

) {
}
package com.laforesta.api.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(

        @NotNull
        UUID reservationId,

        String promoCode

) {
}
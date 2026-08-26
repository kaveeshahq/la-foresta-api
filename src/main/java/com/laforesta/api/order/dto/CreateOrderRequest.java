package com.laforesta.api.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(

        @NotNull(message = "Reservation ID is required")
        UUID reservationId

) {
}
package com.laforesta.api.ticket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateTicketTypeRequest(

        @NotBlank(message = "Ticket type name is required")
        @Size(max = 150)
        String name,

        @Size(max = 500)
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Price cannot be negative"
        )
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3)
        String currency,

        @Min(
                value = 1,
                message = "Capacity must be at least 1"
        )
        int capacity,

        @Min(
                value = 1,
                message = "Maximum per order must be at least 1"
        )
        int maxPerOrder,

        OffsetDateTime salesStartAt,

        OffsetDateTime salesEndAt

) {
}
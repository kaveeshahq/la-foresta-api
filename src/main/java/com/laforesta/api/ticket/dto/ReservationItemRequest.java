package com.laforesta.api.ticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReservationItemRequest(

        @NotNull(message = "Ticket type ID is required")
        UUID ticketTypeId,

        @Min(
                value = 1,
                message = "Quantity must be at least 1"
        )
        int quantity

) {
}
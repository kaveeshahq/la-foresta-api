package com.laforesta.api.ticket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateReservationRequest(

        @NotEmpty(message = "At least one ticket is required")
        List<@Valid ReservationItemRequest> items

) {
}
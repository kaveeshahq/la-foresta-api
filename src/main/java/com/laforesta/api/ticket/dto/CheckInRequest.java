package com.laforesta.api.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(

        @NotBlank(message = "QR token is required")
        String qrToken

) {
}
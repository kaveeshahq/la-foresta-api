package com.laforesta.api.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateVenueRequest(

        @NotBlank(message = "Venue name is required")
        @Size(max = 150)
        String name,

        @Size(max = 255)
        String addressLine1,

        @Size(max = 255)
        String addressLine2,

        @Size(max = 100)
        String city,

        @NotBlank(message = "Country is required")
        @Size(max = 100)
        String country,

        BigDecimal latitude,

        BigDecimal longitude

) {
}
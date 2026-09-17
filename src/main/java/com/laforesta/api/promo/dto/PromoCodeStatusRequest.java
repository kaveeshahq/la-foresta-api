package com.laforesta.api.promo.dto;

import jakarta.validation.constraints.NotNull;

public record PromoCodeStatusRequest(

        @NotNull(message = "Active status is required")
        Boolean active

) {
}
package com.laforesta.api.refund.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRefundRequest(

        @NotBlank
        String reason

) {
}
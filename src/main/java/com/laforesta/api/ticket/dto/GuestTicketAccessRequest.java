package com.laforesta.api.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record GuestTicketAccessRequest(

        @NotBlank
        String accessToken

) {
}
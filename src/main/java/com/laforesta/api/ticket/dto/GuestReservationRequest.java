package com.laforesta.api.ticket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GuestReservationRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String guestEmail,

        @NotBlank
        @Size(max = 150)
        String guestName,

        @NotEmpty
        List<@Valid ReservationItemRequest> items

) {
}
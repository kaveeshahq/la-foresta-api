package com.laforesta.api.ticket.controller;

import com.laforesta.api.ticket.dto.CreateReservationRequest;
import com.laforesta.api.ticket.dto.GuestReservationRequest;
import com.laforesta.api.ticket.dto.ReservationResponse;
import com.laforesta.api.ticket.service.TicketReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest/reservations")
@RequiredArgsConstructor
public class GuestReservationController {

    private final TicketReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createGuestReservation(
            @Valid @RequestBody GuestReservationRequest request
    ) {

        CreateReservationRequest reservationRequest =
                new CreateReservationRequest(
                        request.items()
                );

        ReservationResponse response =
                reservationService.createGuestReservation(
                        request.guestEmail(),
                        request.guestName(),
                        reservationRequest
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
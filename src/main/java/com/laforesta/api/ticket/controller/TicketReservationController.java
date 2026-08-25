package com.laforesta.api.ticket.controller;

import com.laforesta.api.ticket.dto.CreateReservationRequest;
import com.laforesta.api.ticket.dto.ReservationResponse;
import com.laforesta.api.ticket.service.TicketReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class TicketReservationController {

    private final TicketReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse>
    createReservation(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReservationRequest request
    ) {

        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        ReservationResponse response =
                reservationService
                        .createReservation(
                                userId,
                                request
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
package com.laforesta.api.ticket.controller;

import com.laforesta.api.ticket.dto.TicketResponse;
import com.laforesta.api.ticket.service.TicketQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketQueryService ticketQueryService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId =
                UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                ticketQueryService
                        .getTicketsForUser(userId)
        );
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {

        UUID userId =
                UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                ticketQueryService
                        .getTicketForUser(
                                userId,
                                ticketId
                        )
        );
    }
}
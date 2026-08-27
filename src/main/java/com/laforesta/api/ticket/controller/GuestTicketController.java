package com.laforesta.api.ticket.controller;

import com.laforesta.api.ticket.dto.GuestTicketAccessRequest;
import com.laforesta.api.ticket.dto.TicketResponse;
import com.laforesta.api.ticket.service.TicketQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest/tickets")
@RequiredArgsConstructor
public class GuestTicketController {

    private final TicketQueryService ticketQueryService;

    @PostMapping("/access")
    public ResponseEntity<List<TicketResponse>> accessTickets(
            @Valid @RequestBody GuestTicketAccessRequest request
    ) {

        return ResponseEntity.ok(
                ticketQueryService
                        .getGuestTickets(
                                request.accessToken()
                        )
        );
    }
}
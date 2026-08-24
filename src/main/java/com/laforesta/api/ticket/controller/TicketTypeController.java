package com.laforesta.api.ticket.controller;

import com.laforesta.api.ticket.dto.TicketTypeResponse;
import com.laforesta.api.ticket.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping
    public ResponseEntity<List<TicketTypeResponse>>
    getTicketTypes(
            @PathVariable UUID eventId
    ) {

        return ResponseEntity.ok(
                ticketTypeService
                        .getPublicTicketTypes(eventId)
        );
    }
}
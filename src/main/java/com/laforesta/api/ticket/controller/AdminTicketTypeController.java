package com.laforesta.api.ticket.controller;

import com.laforesta.api.ticket.dto.CreateTicketTypeRequest;
import com.laforesta.api.ticket.dto.TicketTypeResponse;
import com.laforesta.api.ticket.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/events/{eventId}/ticket-types")
@RequiredArgsConstructor
public class AdminTicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @PostMapping
    public ResponseEntity<TicketTypeResponse> createTicketType(
            @PathVariable UUID eventId,
            @Valid @RequestBody CreateTicketTypeRequest request
    ) {

        TicketTypeResponse response =
                ticketTypeService.createTicketType(
                        eventId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
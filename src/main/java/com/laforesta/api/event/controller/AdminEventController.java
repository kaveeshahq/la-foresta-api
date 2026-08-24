package com.laforesta.api.event.controller;

import com.laforesta.api.event.dto.CreateEventRequest;
import com.laforesta.api.event.dto.EventResponse;
import com.laforesta.api.event.dto.UpdateEventRequest;
import com.laforesta.api.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request
    ) {

        EventResponse response =
                eventService.createEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request
    ) {

        return ResponseEntity.ok(
                eventService.updateEvent(
                        id,
                        request
                )
        );
    }

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @PostMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                eventService.publishEvent(id)
        );
    }
}
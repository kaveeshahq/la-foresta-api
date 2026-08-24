package com.laforesta.api.event.controller;

import com.laforesta.api.event.dto.EventResponse;
import com.laforesta.api.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> getPublishedEvents() {

        return ResponseEntity.ok(
                eventService.getPublishedEvents()
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<EventResponse> getPublishedEventBySlug(
            @PathVariable String slug
    ) {

        return ResponseEntity.ok(
                eventService.getPublishedEventBySlug(slug)
        );
    }
}
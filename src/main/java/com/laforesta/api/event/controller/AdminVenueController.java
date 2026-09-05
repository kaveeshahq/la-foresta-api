package com.laforesta.api.event.controller;

import com.laforesta.api.event.dto.CreateVenueRequest;
import com.laforesta.api.event.dto.VenueResponse;
import com.laforesta.api.event.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/venues")
@RequiredArgsConstructor
public class AdminVenueController {

    private final VenueService venueService;

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @GetMapping
    public ResponseEntity<List<VenueResponse>> getVenues() {

        return ResponseEntity.ok(
                venueService.getAdminVenues()
        );
    }

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(
            @Valid @RequestBody CreateVenueRequest request
    ) {

        VenueResponse response =
                venueService.createVenue(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
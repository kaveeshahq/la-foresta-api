package com.laforesta.api.event.dto;

import com.laforesta.api.event.model.EventStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventResponse(

        UUID id,
        UUID venueId,
        String venueName,
        String title,
        String slug,
        String shortDescription,
        String description,
        EventStatus status,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        OffsetDateTime salesStartAt,
        OffsetDateTime salesEndAt,
        int minimumAge,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
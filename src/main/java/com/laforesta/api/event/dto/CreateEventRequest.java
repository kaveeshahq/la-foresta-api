package com.laforesta.api.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateEventRequest(

        @NotNull(message = "Venue ID is required")
        UUID venueId,

        @NotBlank(message = "Event title is required")
        @Size(max = 200)
        String title,

        @NotBlank(message = "Slug is required")
        @Size(max = 220)
        String slug,

        @Size(max = 500)
        String shortDescription,

        String description,

        @NotNull(message = "Event start time is required")
        OffsetDateTime startsAt,

        OffsetDateTime endsAt,

        OffsetDateTime salesStartAt,

        OffsetDateTime salesEndAt,

        @Min(value = 0, message = "Minimum age cannot be negative")
        Integer minimumAge

) {
}
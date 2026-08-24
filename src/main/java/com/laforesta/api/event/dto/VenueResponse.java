package com.laforesta.api.event.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VenueResponse(

        UUID id,
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
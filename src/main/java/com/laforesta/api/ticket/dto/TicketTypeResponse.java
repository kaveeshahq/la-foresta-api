package com.laforesta.api.ticket.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketTypeResponse(

        UUID id,
        UUID eventId,
        String eventTitle,
        String name,
        String description,
        BigDecimal price,
        String currency,
        int capacity,
        int maxPerOrder,
        OffsetDateTime salesStartAt,
        OffsetDateTime salesEndAt,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
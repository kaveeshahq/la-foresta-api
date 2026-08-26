package com.laforesta.api.ticket.dto;

import com.laforesta.api.ticket.model.TicketStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketResponse(

        UUID ticketId,
        String ticketNumber,
        String qrToken,
        TicketStatus status,

        UUID orderId,

        UUID ticketTypeId,
        String ticketTypeName,

        UUID eventId,
        String eventTitle,
        String eventSlug,

        OffsetDateTime eventStartsAt,

        OffsetDateTime createdAt

) {
}
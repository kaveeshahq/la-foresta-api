package com.laforesta.api.ticket.dto;

import com.laforesta.api.ticket.model.TicketStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminTicketLookupResponse(

        UUID ticketId,
        String ticketNumber,
        TicketStatus status,

        UUID orderId,

        UUID userId,
        String customerEmail,
        String customerName,

        UUID ticketTypeId,
        String ticketTypeName,

        UUID eventId,
        String eventTitle,

        OffsetDateTime createdAt

) {
}
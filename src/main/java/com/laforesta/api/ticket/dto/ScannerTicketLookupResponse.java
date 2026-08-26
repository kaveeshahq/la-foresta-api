package com.laforesta.api.ticket.dto;

import com.laforesta.api.ticket.model.TicketStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScannerTicketLookupResponse(

        UUID ticketId,
        String ticketNumber,
        TicketStatus status,
        String ticketTypeName,
        String eventTitle,
        String attendeeEmail,
        OffsetDateTime checkedInAt

) {
}
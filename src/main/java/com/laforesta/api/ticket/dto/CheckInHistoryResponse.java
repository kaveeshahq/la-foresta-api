package com.laforesta.api.ticket.dto;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckInHistoryResponse(

        UUID checkInId,
        UUID ticketId,
        String ticketNumber,
        String ticketTypeName,
        String attendeeEmail,
        String scannedByEmail,
        OffsetDateTime checkedInAt

) {
}
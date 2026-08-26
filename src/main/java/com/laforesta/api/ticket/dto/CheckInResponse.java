package com.laforesta.api.ticket.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckInResponse(

        UUID ticketId,
        String ticketNumber,
        String ticketTypeName,
        String eventTitle,
        String result,
        OffsetDateTime checkedInAt

) {
}
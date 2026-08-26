package com.laforesta.api.ticket.dto;

import java.util.UUID;

public record AttendanceSummaryResponse(
        UUID eventId,
        String eventTitle,
        long ticketsIssued,
        long checkedIn,
        long remaining
) {
}
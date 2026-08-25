package com.laforesta.api.ticket.dto;

import com.laforesta.api.ticket.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(

        UUID reservationId,
        ReservationStatus status,
        OffsetDateTime expiresAt,
        List<ReservationItemResponse> items,
        BigDecimal totalAmount,
        String currency

) {
}
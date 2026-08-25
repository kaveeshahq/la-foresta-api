package com.laforesta.api.ticket.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationItemResponse(

        UUID ticketTypeId,
        String ticketTypeName,
        int quantity,
        BigDecimal unitPrice,
        String currency,
        BigDecimal lineTotal

) {
}
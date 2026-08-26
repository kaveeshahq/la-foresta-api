package com.laforesta.api.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(

        UUID ticketTypeId,
        String ticketTypeName,
        int quantity,
        BigDecimal unitPrice,
        String currency,
        BigDecimal lineTotal

) {
}
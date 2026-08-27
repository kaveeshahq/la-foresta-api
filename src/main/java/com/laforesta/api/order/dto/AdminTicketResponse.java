package com.laforesta.api.order.dto;

import com.laforesta.api.ticket.model.TicketStatus;

import java.util.UUID;

public record AdminTicketResponse(

        UUID ticketId,
        String ticketNumber,
        TicketStatus status,
        UUID ticketTypeId,
        String ticketTypeName

) {
}
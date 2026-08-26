package com.laforesta.api.ticket.service;

import com.laforesta.api.ticket.dto.TicketResponse;
import com.laforesta.api.ticket.entity.Ticket;
import com.laforesta.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketQueryService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForUser(
            UUID userId
    ) {

        return ticketRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketForUser(
            UUID userId,
            UUID ticketId
    ) {

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ticket not found"
                        )
                );

        if (ticket.getUser() == null
                || !ticket.getUser()
                .getId()
                .equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ticket not found"
            );
        }

        return toResponse(ticket);
    }

    private TicketResponse toResponse(
            Ticket ticket
    ) {

        var ticketType =
                ticket.getTicketType();

        var event =
                ticketType.getEvent();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getQrToken(),
                ticket.getStatus(),

                ticket.getOrder().getId(),

                ticketType.getId(),
                ticketType.getName(),

                event.getId(),
                event.getTitle(),
                event.getSlug(),

                event.getStartsAt(),

                ticket.getCreatedAt()
        );
    }
}
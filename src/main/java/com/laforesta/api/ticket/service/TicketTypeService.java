package com.laforesta.api.ticket.service;

import com.laforesta.api.event.entity.Event;
import com.laforesta.api.event.model.EventStatus;
import com.laforesta.api.event.repository.EventRepository;
import com.laforesta.api.ticket.dto.CreateTicketTypeRequest;
import com.laforesta.api.ticket.dto.TicketTypeResponse;
import com.laforesta.api.ticket.entity.TicketType;
import com.laforesta.api.ticket.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    @Transactional
    public TicketTypeResponse createTicketType(
            UUID eventId,
            CreateTicketTypeRequest request
    ) {

        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Event not found"
                        )
                );

        String name = request.name().trim();

        if (ticketTypeRepository
                .existsByEventAndNameIgnoreCase(
                        event,
                        name
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A ticket type with this name already exists for the event"
            );
        }

        validateSalesWindow(
                request.salesStartAt(),
                request.salesEndAt(),
                event
        );

        TicketType ticketType =
                new TicketType();

        ticketType.setEvent(event);
        ticketType.setName(name);

        ticketType.setDescription(
                trimToNull(request.description())
        );

        ticketType.setPrice(request.price());

        ticketType.setCurrency(
                request.currency()
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );

        ticketType.setCapacity(
                request.capacity()
        );

        ticketType.setMaxPerOrder(
                request.maxPerOrder()
        );

        ticketType.setSalesStartAt(
                request.salesStartAt()
        );

        ticketType.setSalesEndAt(
                request.salesEndAt()
        );

        ticketType.setActive(true);

        TicketType savedTicketType =
                ticketTypeRepository.save(ticketType);

        return toResponse(savedTicketType);
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getPublicTicketTypes(
            UUID eventId
    ) {

        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Event not found"
                        )
                );

        if (event.getStatus()
                != EventStatus.PUBLISHED) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event not found"
            );
        }

        return ticketTypeRepository
                .findAllByEventAndActiveTrueOrderByPriceAsc(
                        event
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateSalesWindow(
            java.time.OffsetDateTime salesStartAt,
            java.time.OffsetDateTime salesEndAt,
            Event event
    ) {

        if (salesStartAt != null
                && salesEndAt != null
                && !salesEndAt.isAfter(salesStartAt)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket sales end time must be after sales start time"
            );
        }

        if (salesEndAt != null
                && salesEndAt.isAfter(event.getStartsAt())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket sales cannot end after the event starts"
            );
        }
    }

    private TicketTypeResponse toResponse(
            TicketType ticketType
    ) {

        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getEvent().getId(),
                ticketType.getEvent().getTitle(),
                ticketType.getName(),
                ticketType.getDescription(),
                ticketType.getPrice(),
                ticketType.getCurrency(),
                ticketType.getCapacity(),
                ticketType.getMaxPerOrder(),
                ticketType.getSalesStartAt(),
                ticketType.getSalesEndAt(),
                ticketType.isActive(),
                ticketType.getCreatedAt(),
                ticketType.getUpdatedAt()
        );
    }

    private String trimToNull(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}
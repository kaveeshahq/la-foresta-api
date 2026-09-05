package com.laforesta.api.event.service;

import com.laforesta.api.event.dto.CreateEventRequest;
import com.laforesta.api.event.dto.EventResponse;
import com.laforesta.api.event.dto.UpdateEventRequest;
import com.laforesta.api.event.entity.Event;
import com.laforesta.api.event.entity.Venue;
import com.laforesta.api.event.model.EventStatus;
import com.laforesta.api.event.repository.EventRepository;
import com.laforesta.api.event.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public EventResponse createEvent(
            CreateEventRequest request
    ) {

        Venue venue = venueRepository
                .findById(request.venueId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Venue not found"
                        )
                );

        String slug = request.slug()
                .trim()
                .toLowerCase();

        if (eventRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An event with this slug already exists"
            );
        }

        validateDates(
                request.startsAt(),
                request.endsAt(),
                request.salesStartAt(),
                request.salesEndAt()
        );

        Event event = new Event();

        event.setVenue(venue);
        event.setTitle(request.title().trim());
        event.setSlug(slug);

        event.setShortDescription(
                trimToNull(request.shortDescription())
        );

        event.setDescription(
                trimToNull(request.description())
        );

        event.setStartsAt(request.startsAt());
        event.setEndsAt(request.endsAt());
        event.setSalesStartAt(request.salesStartAt());
        event.setSalesEndAt(request.salesEndAt());

        event.setMinimumAge(
                request.minimumAge() != null
                        ? request.minimumAge()
                        : 18
        );

        event.setStatus(EventStatus.DRAFT);

        Event savedEvent =
                eventRepository.save(event);

        return toResponse(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(
            UUID eventId,
            UpdateEventRequest request
    ) {

        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Event not found"
                        )
                );

        Venue venue = venueRepository
                .findById(request.venueId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Venue not found"
                        )
                );

        String slug = request.slug()
                .trim()
                .toLowerCase();

        if (eventRepository.existsBySlugAndIdNot(
                slug,
                eventId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An event with this slug already exists"
            );
        }

        validateDates(
                request.startsAt(),
                request.endsAt(),
                request.salesStartAt(),
                request.salesEndAt()
        );

        event.setVenue(venue);
        event.setTitle(request.title().trim());
        event.setSlug(slug);

        event.setShortDescription(
                trimToNull(request.shortDescription())
        );

        event.setDescription(
                trimToNull(request.description())
        );

        event.setStartsAt(request.startsAt());
        event.setEndsAt(request.endsAt());
        event.setSalesStartAt(request.salesStartAt());
        event.setSalesEndAt(request.salesEndAt());

        event.setMinimumAge(
                request.minimumAge() != null
                        ? request.minimumAge()
                        : 18
        );

        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAdminEvents() {

        return eventRepository
                .findAll(
                        Sort.by(
                                Sort.Direction.DESC,
                                "startsAt"
                        )
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getPublishedEvents() {

        return eventRepository
                .findAllByStatusOrderByStartsAtAsc(
                        EventStatus.PUBLISHED
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getPublishedEventBySlug(
            String slug
    ) {

        Event event = eventRepository
                .findBySlug(
                        slug.trim().toLowerCase()
                )
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

        return toResponse(event);
    }

    @Transactional
    public EventResponse publishEvent(
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
                == EventStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cancelled events cannot be published"
            );
        }

        if (event.getStatus()
                == EventStatus.COMPLETED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed events cannot be published"
            );
        }

        if (event.getStatus()
                == EventStatus.PUBLISHED) {

            return toResponse(event);
        }

        event.setStatus(EventStatus.PUBLISHED);

        return toResponse(event);
    }

    private void validateDates(
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime salesStartAt,
            OffsetDateTime salesEndAt
    ) {

        if (endsAt != null
                && !endsAt.isAfter(startsAt)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Event end time must be after start time"
            );
        }

        if (salesStartAt != null
                && salesEndAt != null
                && !salesEndAt.isAfter(salesStartAt)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sales end time must be after sales start time"
            );
        }

        if (salesEndAt != null
                && salesEndAt.isAfter(startsAt)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket sales cannot end after the event starts"
            );
        }
    }

    private EventResponse toResponse(
            Event event
    ) {

        return new EventResponse(
                event.getId(),

                event.getVenue() != null
                        ? event.getVenue().getId()
                        : null,

                event.getVenue() != null
                        ? event.getVenue().getName()
                        : null,

                event.getTitle(),
                event.getSlug(),
                event.getShortDescription(),
                event.getDescription(),
                event.getStatus(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getSalesStartAt(),
                event.getSalesEndAt(),
                event.getMinimumAge(),
                event.getCreatedAt(),
                event.getUpdatedAt()
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
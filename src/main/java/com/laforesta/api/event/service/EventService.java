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

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
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

        Venue venue =
                findVenue(request.venueId());

        String slug =
                normalizeSlug(request.slug());

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
                trimToNull(
                        request.shortDescription()
                )
        );

        event.setDescription(
                trimToNull(
                        request.description()
                )
        );

        event.setCardImageUrl(
                normalizeImageUrl(
                        request.cardImageUrl(),
                        "Card image URL"
                )
        );

        event.setHeroImageUrl(
                normalizeImageUrl(
                        request.heroImageUrl(),
                        "Hero image URL"
                )
        );

        event.setStartsAt(
                request.startsAt()
        );

        event.setEndsAt(
                request.endsAt()
        );

        event.setSalesStartAt(
                request.salesStartAt()
        );

        event.setSalesEndAt(
                request.salesEndAt()
        );

        event.setMinimumAge(
                request.minimumAge() != null
                        ? request.minimumAge()
                        : 18
        );

        event.setStatus(
                EventStatus.DRAFT
        );

        Event savedEvent =
                eventRepository.save(event);

        return toResponse(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(
            UUID eventId,
            UpdateEventRequest request
    ) {

        Event event =
                findEvent(eventId);

        Venue venue =
                findVenue(request.venueId());

        String slug =
                normalizeSlug(request.slug());

        if (eventRepository
                .existsBySlugAndIdNot(
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
                trimToNull(
                        request.shortDescription()
                )
        );

        event.setDescription(
                trimToNull(
                        request.description()
                )
        );

        event.setCardImageUrl(
                normalizeImageUrl(
                        request.cardImageUrl(),
                        "Card image URL"
                )
        );

        event.setHeroImageUrl(
                normalizeImageUrl(
                        request.heroImageUrl(),
                        "Hero image URL"
                )
        );

        event.setStartsAt(
                request.startsAt()
        );

        event.setEndsAt(
                request.endsAt()
        );

        event.setSalesStartAt(
                request.salesStartAt()
        );

        event.setSalesEndAt(
                request.salesEndAt()
        );

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

        Event event =
                eventRepository
                        .findBySlug(
                                normalizeSlug(slug)
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

        Event event =
                findEvent(eventId);

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

        event.setStatus(
                EventStatus.PUBLISHED
        );

        return toResponse(event);
    }

    private Event findEvent(
            UUID eventId
    ) {

        return eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Event not found"
                        )
                );
    }

    private Venue findVenue(
            UUID venueId
    ) {

        return venueRepository
                .findById(venueId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Venue not found"
                        )
                );
    }

    private String normalizeSlug(
            String slug
    ) {

        return slug
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeImageUrl(
            String value,
            String fieldName
    ) {

        String normalized =
                trimToNull(value);

        if (normalized == null) {
            return null;
        }

        /*
         * Permit assets served by the frontend, for example:
         * /media/events/eclipse-2026.jpg
         *
         * Protocol-relative URLs beginning with // are rejected.
         */
        if (normalized.startsWith("/")
                && !normalized.startsWith("//")) {

            return normalized;
        }

        try {

            URI uri =
                    URI.create(normalized);

            String scheme =
                    uri.getScheme();

            boolean validScheme =
                    scheme != null
                            && (
                            scheme.equalsIgnoreCase("https")
                                    || scheme.equalsIgnoreCase("http")
                    );

            if (!validScheme
                    || uri.getHost() == null
                    || uri.getHost().isBlank()) {

                throw invalidImageUrl(
                        fieldName
                );
            }

            return normalized;

        } catch (IllegalArgumentException exception) {

            throw invalidImageUrl(
                    fieldName
            );
        }
    }

    private ResponseStatusException invalidImageUrl(
            String fieldName
    ) {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                fieldName
                        + " must be an http(s) URL or a frontend path beginning with /"
        );
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
                && !salesEndAt.isAfter(
                salesStartAt
        )) {

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

                event.getCardImageUrl(),
                event.getHeroImageUrl(),

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

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}
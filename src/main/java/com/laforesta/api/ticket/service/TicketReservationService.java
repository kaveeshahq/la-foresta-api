package com.laforesta.api.ticket.service;

import com.laforesta.api.ticket.dto.CreateReservationRequest;
import com.laforesta.api.ticket.dto.ReservationItemRequest;
import com.laforesta.api.ticket.dto.ReservationItemResponse;
import com.laforesta.api.ticket.dto.ReservationResponse;
import com.laforesta.api.ticket.entity.TicketReservation;
import com.laforesta.api.ticket.entity.TicketReservationItem;
import com.laforesta.api.ticket.entity.TicketType;
import com.laforesta.api.ticket.model.ReservationStatus;
import com.laforesta.api.ticket.repository.TicketReservationItemRepository;
import com.laforesta.api.ticket.repository.TicketReservationRepository;
import com.laforesta.api.ticket.repository.TicketTypeRepository;
import com.laforesta.api.user.entity.User;
import com.laforesta.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketReservationService {

    private static final int RESERVATION_MINUTES = 10;

    private final TicketReservationRepository reservationRepository;
    private final TicketReservationItemRepository reservationItemRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;

    /*
     * Registered-user reservation
     */
    @Transactional
    public ReservationResponse createReservation(
            UUID userId,
            CreateReservationRequest request
    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        return createReservationInternal(
                user,
                null,
                null,
                request
        );
    }

    /*
     * Guest reservation
     */
    @Transactional
    public ReservationResponse createGuestReservation(
            String guestEmail,
            String guestName,
            CreateReservationRequest request
    ) {

        validateGuestDetails(
                guestEmail,
                guestName
        );

        return createReservationInternal(
                null,
                guestEmail.trim().toLowerCase(),
                guestName.trim(),
                request
        );
    }

    /*
     * Shared reservation creation logic.
     *
     * Both registered and guest checkout use exactly
     * the same inventory-locking and pricing logic.
     */
    private ReservationResponse createReservationInternal(
            User user,
            String guestEmail,
            String guestName,
            CreateReservationRequest request
    ) {

        validateDuplicateTicketTypes(
                request
        );

        OffsetDateTime now =
                OffsetDateTime.now();

        TicketReservation reservation =
                new TicketReservation();

        reservation.setUser(user);
        reservation.setGuestEmail(guestEmail);
        reservation.setGuestName(guestName);

        reservation.setStatus(
                ReservationStatus.ACTIVE
        );

        reservation.setExpiresAt(
                now.plusMinutes(
                        RESERVATION_MINUTES
                )
        );

        List<ReservationItemResponse> responseItems =
                new ArrayList<>();

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        String reservationCurrency =
                null;

        UUID eventId =
                null;

        for (ReservationItemRequest requestedItem
                : request.items()) {

            TicketType ticketType =
                    ticketTypeRepository
                            .findByIdForUpdate(
                                    requestedItem
                                            .ticketTypeId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Ticket type not found"
                                    )
                            );

            validateTicketTypeAvailability(
                    ticketType,
                    requestedItem.quantity(),
                    now
            );

            /*
             * A reservation cannot mix events.
             */
            if (eventId == null) {

                eventId =
                        ticketType
                                .getEvent()
                                .getId();

            } else if (!eventId.equals(
                    ticketType
                            .getEvent()
                            .getId()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "All ticket types in a reservation must belong to the same event"
                );
            }

            /*
             * A reservation cannot mix currencies.
             */
            if (reservationCurrency == null) {

                reservationCurrency =
                        ticketType.getCurrency();

            } else if (!reservationCurrency.equals(
                    ticketType.getCurrency()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "All ticket types in a reservation must use the same currency"
                );
            }

            TicketReservationItem item =
                    new TicketReservationItem();

            item.setTicketType(
                    ticketType
            );

            item.setQuantity(
                    requestedItem.quantity()
            );

            item.setUnitPrice(
                    ticketType.getPrice()
            );

            item.setCurrency(
                    ticketType.getCurrency()
            );

            reservation.addItem(
                    item
            );

            BigDecimal lineTotal =
                    ticketType
                            .getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            requestedItem
                                                    .quantity()
                                    )
                            );

            totalAmount =
                    totalAmount.add(
                            lineTotal
                    );

            responseItems.add(
                    new ReservationItemResponse(
                            ticketType.getId(),
                            ticketType.getName(),
                            requestedItem.quantity(),
                            ticketType.getPrice(),
                            ticketType.getCurrency(),
                            lineTotal
                    )
            );
        }

        TicketReservation savedReservation =
                reservationRepository.save(
                        reservation
                );

        return new ReservationResponse(
                savedReservation.getId(),
                savedReservation.getStatus(),
                savedReservation.getExpiresAt(),
                responseItems,
                totalAmount,
                reservationCurrency
        );
    }

    private void validateGuestDetails(
            String guestEmail,
            String guestName
    ) {

        if (guestEmail == null
                || guestEmail.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest email is required"
            );
        }

        if (guestName == null
                || guestName.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest name is required"
            );
        }

        if (guestEmail.length() > 255) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest email is too long"
            );
        }

        if (guestName.length() > 150) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest name is too long"
            );
        }
    }

    private void validateTicketTypeAvailability(
            TicketType ticketType,
            int requestedQuantity,
            OffsetDateTime now
    ) {

        if (!ticketType.isActive()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket type is not active"
            );
        }

        if (requestedQuantity
                > ticketType.getMaxPerOrder()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested quantity exceeds the maximum allowed per order"
            );
        }

        if (ticketType.getSalesStartAt() != null
                && now.isBefore(
                ticketType.getSalesStartAt()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket sales have not started yet"
            );
        }

        if (ticketType.getSalesEndAt() != null
                && now.isAfter(
                ticketType.getSalesEndAt()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket sales have ended"
            );
        }

        long reservedQuantity =
                reservationItemRepository
                        .sumReservedAndConfirmedQuantity(
                                ticketType.getId(),
                                ReservationStatus.ACTIVE,
                                ReservationStatus.CONFIRMED,
                                now
                        );

        long availableQuantity =
                (long) ticketType.getCapacity()
                        - reservedQuantity;

        if (requestedQuantity
                > availableQuantity) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Not enough tickets available"
            );
        }
    }

    private void validateDuplicateTicketTypes(
            CreateReservationRequest request
    ) {

        Set<UUID> ticketTypeIds =
                new HashSet<>();

        for (ReservationItemRequest item
                : request.items()) {

            if (!ticketTypeIds.add(
                    item.ticketTypeId()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "The same ticket type cannot appear more than once in a reservation"
                );
            }
        }
    }
}
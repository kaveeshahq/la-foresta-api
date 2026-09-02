package com.laforesta.api.ticket.service;

import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.order.service.GuestAccessTokenService;
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
    private final OrderRepository orderRepository;
    private final GuestAccessTokenService guestAccessTokenService;

    /*
     * Registered customer's ticket list
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForUser(
            UUID userId
    ) {

        return ticketRepository
                .findAllByUserIdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * Registered customer's individual ticket
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicketForUser(
            UUID userId,
            UUID ticketId
    ) {

        Ticket ticket =
                ticketRepository
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

        return toResponse(
                ticket
        );
    }

    /*
     * Guest ticket access.
     *
     * The raw token supplied by the guest is hashed,
     * then compared against the hash stored on the order.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getGuestTickets(
            String accessToken
    ) {

        if (accessToken == null
                || accessToken.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest access token is required"
            );
        }

        String tokenHash =
                guestAccessTokenService
                        .hashToken(
                                accessToken
                        );

        Order order =
                orderRepository
                        .findByGuestAccessTokenHash(
                                tokenHash
                        )
                        .or(() ->
                                orderRepository
                                        .findByGuestEmailAccessTokenHash(
                                                tokenHash
                                        )
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Guest purchase not found"
                                )
                        );

        /*
         * Extra protection:
         * this endpoint must never expose tickets
         * belonging to registered users.
         */
        if (order.getUser() != null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Guest purchase not found"
            );
        }

        /*
         * Tickets should only be available after
         * successful payment.
         */
        if (order.getStatus()
                != OrderStatus.PAID) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order has not been paid"
            );
        }

        List<Ticket> tickets =
                ticketRepository
                        .findAllByOrderId(
                                order.getId()
                        );

        /*
         * A PAID order should normally already have
         * tickets because ticket issuance happens during
         * payment confirmation.
         */
        if (tickets.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No tickets found for this order"
            );
        }

        return tickets
                .stream()
                .map(this::toResponse)
                .toList();
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

                ticket.getOrder()
                        .getId(),

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
package com.laforesta.api.order.service;

import com.laforesta.api.order.dto.AdminOrderSummaryResponse;
import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.ticket.dto.AdminTicketLookupResponse;
import com.laforesta.api.ticket.entity.Ticket;
import com.laforesta.api.ticket.repository.TicketRepository;
import com.laforesta.api.user.dto.AdminCustomerResponse;
import com.laforesta.api.user.entity.User;
import com.laforesta.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSearchService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<AdminOrderSummaryResponse> findOrdersByEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        return orderRepository
                .findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(
                        email.trim()
                )
                .stream()
                .map(this::toOrderSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCustomerResponse findCustomerByEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        User user = userRepository
                .findByEmailIgnoreCase(
                        email.trim()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Customer not found"
                        )
                );

        return new AdminCustomerResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isEmailVerified(),
                user.getAccountStatus(),
                user.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public AdminTicketLookupResponse findTicketByNumber(
            String ticketNumber
    ) {

        if (ticketNumber == null
                || ticketNumber.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket number is required"
            );
        }

        Ticket ticket = ticketRepository
                .findByTicketNumberIgnoreCase(
                        ticketNumber.trim()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ticket not found"
                        )
                );

        User user = ticket.getUser();

        return new AdminTicketLookupResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getStatus(),

                ticket.getOrder().getId(),

                user != null
                        ? user.getId()
                        : null,

                user != null
                        ? user.getEmail()
                        : null,

                user != null
                        ? user.getFullName()
                        : null,

                ticket.getTicketType().getId(),
                ticket.getTicketType().getName(),

                ticket.getTicketType()
                        .getEvent()
                        .getId(),

                ticket.getTicketType()
                        .getEvent()
                        .getTitle(),

                ticket.getCreatedAt()
        );
    }

    private AdminOrderSummaryResponse toOrderSummary(
            Order order
    ) {

        return new AdminOrderSummaryResponse(
                order.getId(),

                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getUser().getFullName(),

                order.getStatus(),

                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getTotalAmount(),

                order.getPromoCodeSnapshot(),
                order.getCurrency(),

                order.getCreatedAt()
        );
    }
}
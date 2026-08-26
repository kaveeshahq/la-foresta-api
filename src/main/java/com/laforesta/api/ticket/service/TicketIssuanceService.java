package com.laforesta.api.ticket.service;

import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.entity.OrderItem;
import com.laforesta.api.ticket.entity.Ticket;
import com.laforesta.api.ticket.model.TicketStatus;
import com.laforesta.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketIssuanceService {

    private final TicketRepository ticketRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void issueTicketsForOrder(Order order) {

        long expectedTicketCount = order.getItems()
                .stream()
                .mapToLong(OrderItem::getQuantity)
                .sum();

        long existingTicketCount =
                ticketRepository.countByOrderId(
                        order.getId()
                );

        if (existingTicketCount == expectedTicketCount) {
            return;
        }

        if (existingTicketCount > 0) {
            throw new IllegalStateException(
                    "Partial ticket issuance detected for order "
                            + order.getId()
            );
        }

        for (OrderItem orderItem : order.getItems()) {

            for (int i = 0;
                 i < orderItem.getQuantity();
                 i++) {

                Ticket ticket = new Ticket();

                ticket.setOrder(order);

                ticket.setTicketType(
                        orderItem.getTicketType()
                );

                ticket.setUser(
                        order.getUser()
                );

                ticket.setTicketNumber(
                        generateTicketNumber()
                );

                ticket.setQrToken(
                        generateQrToken()
                );

                ticket.setStatus(
                        TicketStatus.VALID
                );

                ticketRepository.save(ticket);
            }
        }
    }

    private String generateTicketNumber() {

        String value =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 10)
                        .toUpperCase();

        return "LF-" + value;
    }

    private String generateQrToken() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
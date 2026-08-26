package com.laforesta.api.ticket.repository;

import com.laforesta.api.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository
        extends JpaRepository<Ticket, UUID> {

    List<Ticket> findAllByOrderId(
            UUID orderId
    );

    List<Ticket> findAllByUserIdOrderByCreatedAtDesc(
            UUID userId
    );

    Optional<Ticket> findByQrToken(
            String qrToken
    );

    boolean existsByTicketNumber(
            String ticketNumber
    );

    long countByOrderId(
            UUID orderId
    );
}
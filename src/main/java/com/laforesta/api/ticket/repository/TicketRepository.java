package com.laforesta.api.ticket.repository;

import com.laforesta.api.ticket.entity.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    Optional<Ticket> findByTicketNumberIgnoreCase(
            String ticketNumber
    );

    boolean existsByTicketNumber(
            String ticketNumber
    );

    long countByOrderId(
            UUID orderId
    );

    long countByTicketTypeEventId(
            UUID eventId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM Ticket t
            WHERE t.qrToken = :qrToken
            """)
    Optional<Ticket> findByQrTokenForUpdate(
            @Param("qrToken") String qrToken
    );
}
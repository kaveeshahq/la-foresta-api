package com.laforesta.api.ticket.repository;

import com.laforesta.api.ticket.entity.TicketReservationItem;
import com.laforesta.api.ticket.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TicketReservationItemRepository
        extends JpaRepository<TicketReservationItem, UUID> {

    @Query("""
           SELECT COALESCE(SUM(item.quantity), 0)
           FROM TicketReservationItem item
           JOIN item.reservation reservation
           WHERE item.ticketType.id = :ticketTypeId
             AND (
                    reservation.status = :confirmedStatus
                    OR (
                        reservation.status = :activeStatus
                        AND reservation.expiresAt > :now
                    )
                 )
           """)
    long sumReservedAndConfirmedQuantity(
            @Param("ticketTypeId") UUID ticketTypeId,
            @Param("activeStatus") ReservationStatus activeStatus,
            @Param("confirmedStatus") ReservationStatus confirmedStatus,
            @Param("now") OffsetDateTime now
    );
}
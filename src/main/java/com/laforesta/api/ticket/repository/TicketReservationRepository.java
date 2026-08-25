package com.laforesta.api.ticket.repository;

import com.laforesta.api.ticket.entity.TicketReservation;
import com.laforesta.api.ticket.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TicketReservationRepository
        extends JpaRepository<TicketReservation, UUID> {

    List<TicketReservation>
    findAllByStatusAndExpiresAtBefore(
            ReservationStatus status,
            OffsetDateTime time
    );
}
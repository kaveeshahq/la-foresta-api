package com.laforesta.api.ticket.repository;

import com.laforesta.api.ticket.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CheckInRepository
        extends JpaRepository<CheckIn, UUID> {

    boolean existsByTicketId(
            UUID ticketId
    );

    Optional<CheckIn> findByTicketId(
            UUID ticketId
    );
}
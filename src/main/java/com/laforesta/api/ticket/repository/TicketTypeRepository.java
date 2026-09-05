package com.laforesta.api.ticket.repository;

import com.laforesta.api.event.entity.Event;
import com.laforesta.api.ticket.entity.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketTypeRepository
        extends JpaRepository<TicketType, UUID> {

    boolean existsByEventAndNameIgnoreCase(
            Event event,
            String name
    );

    List<TicketType> findAllByEventAndActiveTrueOrderByPriceAsc(
            Event event
    );

    List<TicketType> findAllByEventOrderByPriceAsc(
            Event event
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT tt
           FROM TicketType tt
           WHERE tt.id = :id
           """)
    Optional<TicketType> findByIdForUpdate(
            @Param("id") UUID id
    );
}
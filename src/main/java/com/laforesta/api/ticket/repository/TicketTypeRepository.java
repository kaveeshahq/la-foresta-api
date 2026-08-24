package com.laforesta.api.ticket.repository;

import com.laforesta.api.event.entity.Event;
import com.laforesta.api.ticket.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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
}
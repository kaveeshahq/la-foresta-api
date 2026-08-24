package com.laforesta.api.event.repository;

import com.laforesta.api.event.entity.Event;
import com.laforesta.api.event.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository
        extends JpaRepository<Event, UUID> {

    Optional<Event> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(
            String slug,
            UUID id
    );

    List<Event> findAllByStatusOrderByStartsAtAsc(
            EventStatus status
    );
}
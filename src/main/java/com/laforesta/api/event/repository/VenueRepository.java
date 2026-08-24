package com.laforesta.api.event.repository;

import com.laforesta.api.event.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository
        extends JpaRepository<Venue, UUID> {
}
package com.laforesta.api.event.repository;

import com.laforesta.api.event.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface VenueRepository
        extends JpaRepository<Venue, UUID> {

    @Query("""
           SELECT COUNT(venue)
           FROM Venue venue
           WHERE LOWER(venue.name) = LOWER(:name)
             AND LOWER(venue.country) = LOWER(:country)
             AND (
                    (:city IS NULL AND venue.city IS NULL)
                    OR (
                        :city IS NOT NULL
                        AND LOWER(venue.city) = LOWER(:city)
                    )
                 )
             AND (
                    :excludedId IS NULL
                    OR venue.id <> :excludedId
                 )
           """)
    long countPotentialDuplicates(
            @Param("name") String name,
            @Param("city") String city,
            @Param("country") String country,
            @Param("excludedId") UUID excludedId
    );
}
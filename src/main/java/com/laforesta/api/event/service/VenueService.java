package com.laforesta.api.event.service;

import com.laforesta.api.event.dto.CreateVenueRequest;
import com.laforesta.api.event.dto.UpdateVenueRequest;
import com.laforesta.api.event.dto.VenueResponse;
import com.laforesta.api.event.entity.Venue;
import com.laforesta.api.event.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    @Transactional
    public VenueResponse createVenue(
            CreateVenueRequest request
    ) {

        String name = request.name().trim();
        String city = trimToNull(request.city());
        String country = request.country().trim();

        validateDuplicate(
                name,
                city,
                country,
                null
        );

        Venue venue = new Venue();

        venue.setName(name);

        venue.setAddressLine1(
                trimToNull(request.addressLine1())
        );

        venue.setAddressLine2(
                trimToNull(request.addressLine2())
        );

        venue.setCity(city);
        venue.setCountry(country);
        venue.setLatitude(request.latitude());
        venue.setLongitude(request.longitude());

        Venue savedVenue =
                venueRepository.save(venue);

        return toResponse(savedVenue);
    }

    @Transactional
    public VenueResponse updateVenue(
            UUID venueId,
            UpdateVenueRequest request
    ) {

        Venue venue = venueRepository
                .findById(venueId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Venue not found"
                        )
                );

        String name = request.name().trim();
        String city = trimToNull(request.city());
        String country = request.country().trim();

        validateDuplicate(
                name,
                city,
                country,
                venueId
        );

        venue.setName(name);

        venue.setAddressLine1(
                trimToNull(request.addressLine1())
        );

        venue.setAddressLine2(
                trimToNull(request.addressLine2())
        );

        venue.setCity(city);
        venue.setCountry(country);
        venue.setLatitude(request.latitude());
        venue.setLongitude(request.longitude());

        return toResponse(venue);
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> getAdminVenues() {

        return venueRepository
                .findAll(
                        Sort.by(
                                Sort.Direction.ASC,
                                "name"
                        )
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateDuplicate(
            String name,
            String city,
            String country,
            UUID excludedId
    ) {

        long duplicateCount =
                venueRepository
                        .countPotentialDuplicates(
                                name,
                                city,
                                country,
                                excludedId
                        );

        if (duplicateCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A venue with this name and location already exists"
            );
        }
    }

    private VenueResponse toResponse(
            Venue venue
    ) {

        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddressLine1(),
                venue.getAddressLine2(),
                venue.getCity(),
                venue.getCountry(),
                venue.getLatitude(),
                venue.getLongitude(),
                venue.getCreatedAt(),
                venue.getUpdatedAt()
        );
    }

    private String trimToNull(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}
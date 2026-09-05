package com.laforesta.api.event.service;

import com.laforesta.api.event.dto.CreateVenueRequest;
import com.laforesta.api.event.dto.VenueResponse;
import com.laforesta.api.event.entity.Venue;
import com.laforesta.api.event.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    @Transactional
    public VenueResponse createVenue(
            CreateVenueRequest request
    ) {

        Venue venue = new Venue();

        venue.setName(request.name().trim());

        venue.setAddressLine1(
                trimToNull(request.addressLine1())
        );

        venue.setAddressLine2(
                trimToNull(request.addressLine2())
        );

        venue.setCity(
                trimToNull(request.city())
        );

        venue.setCountry(
                request.country().trim()
        );

        venue.setLatitude(
                request.latitude()
        );

        venue.setLongitude(
                request.longitude()
        );

        Venue savedVenue =
                venueRepository.save(venue);

        return toResponse(savedVenue);
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
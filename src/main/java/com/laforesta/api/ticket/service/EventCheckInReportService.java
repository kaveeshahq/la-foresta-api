package com.laforesta.api.ticket.service;

import com.laforesta.api.event.entity.Event;
import com.laforesta.api.event.repository.EventRepository;
import com.laforesta.api.ticket.dto.AttendanceSummaryResponse;
import com.laforesta.api.ticket.dto.CheckInHistoryResponse;
import com.laforesta.api.ticket.dto.ScannerTicketLookupResponse;
import com.laforesta.api.ticket.entity.CheckIn;
import com.laforesta.api.ticket.entity.Ticket;
import com.laforesta.api.ticket.repository.CheckInRepository;
import com.laforesta.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventCheckInReportService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final CheckInRepository checkInRepository;

    @Transactional(readOnly = true)
    public List<CheckInHistoryResponse> getCheckIns(
            UUID eventId
    ) {

        ensureEventExists(eventId);

        return checkInRepository
                .findAllByTicketTicketTypeEventIdOrderByCheckedInAtDesc(
                        eventId
                )
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getAttendanceSummary(
            UUID eventId
    ) {

        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Event not found"
                        )
                );

        long ticketsIssued =
                ticketRepository
                        .countByTicketTypeEventId(
                                eventId
                        );

        long checkedIn =
                checkInRepository
                        .countByTicketTicketTypeEventId(
                                eventId
                        );

        long remaining =
                Math.max(
                        ticketsIssued - checkedIn,
                        0
                );

        return new AttendanceSummaryResponse(
                event.getId(),
                event.getTitle(),
                ticketsIssued,
                checkedIn,
                remaining
        );
    }

    @Transactional(readOnly = true)
    public ScannerTicketLookupResponse lookupTicket(
            String qrToken
    ) {

        Ticket ticket = ticketRepository
                .findByQrToken(
                        qrToken.trim()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Invalid ticket"
                        )
                );

        CheckIn checkIn =
                checkInRepository
                        .findByTicketId(
                                ticket.getId()
                        )
                        .orElse(null);

        return new ScannerTicketLookupResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getStatus(),
                ticket.getTicketType().getName(),
                ticket.getTicketType()
                        .getEvent()
                        .getTitle(),
                ticket.getUser() != null
                        ? ticket.getUser().getEmail()
                        : null,
                checkIn != null
                        ? checkIn.getCheckedInAt()
                        : null
        );
    }

    private CheckInHistoryResponse toHistoryResponse(
            CheckIn checkIn
    ) {

        Ticket ticket = checkIn.getTicket();

        return new CheckInHistoryResponse(
                checkIn.getId(),
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getTicketType().getName(),
                ticket.getUser() != null
                        ? ticket.getUser().getEmail()
                        : null,
                checkIn.getScannedByUser() != null
                        ? checkIn.getScannedByUser().getEmail()
                        : null,
                checkIn.getCheckedInAt()
        );
    }

    private void ensureEventExists(
            UUID eventId
    ) {

        if (!eventRepository.existsById(eventId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event not found"
            );
        }
    }
}
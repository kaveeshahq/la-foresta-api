package com.laforesta.api.ticket.service;

import com.laforesta.api.ticket.dto.CheckInResponse;
import com.laforesta.api.ticket.entity.CheckIn;
import com.laforesta.api.ticket.entity.Ticket;
import com.laforesta.api.ticket.model.TicketStatus;
import com.laforesta.api.ticket.repository.CheckInRepository;
import com.laforesta.api.ticket.repository.TicketRepository;
import com.laforesta.api.user.entity.User;
import com.laforesta.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScannerCheckInService {

    private final TicketRepository ticketRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    @Transactional
    public CheckInResponse checkIn(
            UUID scannerUserId,
            String qrToken
    ) {

        User scannerUser = userRepository
                .findById(scannerUserId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Scanner user not found"
                        )
                );

        Ticket ticket = ticketRepository
                .findByQrTokenForUpdate(
                        qrToken.trim()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Invalid ticket"
                        )
                );

        if (ticket.getStatus() == TicketStatus.USED) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket has already been used"
            );
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket has been cancelled"
            );
        }

        if (ticket.getStatus() == TicketStatus.REFUNDED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket has been refunded"
            );
        }

        if (ticket.getStatus() != TicketStatus.VALID) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket is not valid"
            );
        }

        if (checkInRepository.existsByTicketId(
                ticket.getId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket has already been checked in"
            );
        }

        ticket.setStatus(
                TicketStatus.USED
        );

        CheckIn checkIn = new CheckIn();

        checkIn.setTicket(ticket);
        checkIn.setScannedByUser(scannerUser);

        CheckIn savedCheckIn =
                checkInRepository.save(checkIn);

        return new CheckInResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getTicketType().getName(),
                ticket.getTicketType()
                        .getEvent()
                        .getTitle(),
                "CHECKED_IN",
                savedCheckIn.getCheckedInAt()
        );
    }
}
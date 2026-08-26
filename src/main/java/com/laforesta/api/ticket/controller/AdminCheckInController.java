package com.laforesta.api.ticket.controller;

import com.laforesta.api.ticket.dto.AttendanceSummaryResponse;
import com.laforesta.api.ticket.dto.CheckInHistoryResponse;
import com.laforesta.api.ticket.service.EventCheckInReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminCheckInController {

    private final EventCheckInReportService reportService;

    @GetMapping("/{eventId}/check-ins")
    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<List<CheckInHistoryResponse>> getCheckIns(
            @PathVariable UUID eventId
    ) {

        return ResponseEntity.ok(
                reportService.getCheckIns(eventId)
        );
    }

    @GetMapping("/{eventId}/attendance-summary")
    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<AttendanceSummaryResponse> getAttendanceSummary(
            @PathVariable UUID eventId
    ) {

        return ResponseEntity.ok(
                reportService.getAttendanceSummary(
                        eventId
                )
        );
    }
}
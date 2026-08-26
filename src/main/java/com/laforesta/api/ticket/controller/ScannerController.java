package com.laforesta.api.ticket.controller;

import com.laforesta.api.ticket.dto.CheckInRequest;
import com.laforesta.api.ticket.dto.CheckInResponse;
import com.laforesta.api.ticket.service.ScannerCheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/scanner")
@RequiredArgsConstructor
public class ScannerController {

    private final ScannerCheckInService scannerCheckInService;

    @PostMapping("/check-in")
    @PreAuthorize(
            "hasAnyRole('SCANNER_STAFF','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<CheckInResponse> checkIn(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckInRequest request
    ) {

        UUID scannerUserId =
                UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                scannerCheckInService.checkIn(
                        scannerUserId,
                        request.qrToken()
                )
        );
    }
}
package com.laforesta.api.order.controller;

import com.laforesta.api.order.dto.AdminOrderSummaryResponse;
import com.laforesta.api.order.service.AdminSearchService;
import com.laforesta.api.ticket.dto.AdminTicketLookupResponse;
import com.laforesta.api.user.dto.AdminCustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSearchController {

    private final AdminSearchService adminSearchService;

    @GetMapping("/orders")
    @PreAuthorize(
            "hasAnyRole('FINANCE_MANAGER','SUPPORT_AGENT','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<List<AdminOrderSummaryResponse>>
    findOrdersByEmail(
            @RequestParam String email
    ) {

        return ResponseEntity.ok(
                adminSearchService
                        .findOrdersByEmail(email)
        );
    }

    @GetMapping("/customers")
    @PreAuthorize(
            "hasAnyRole('SUPPORT_AGENT','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<AdminCustomerResponse>
    findCustomerByEmail(
            @RequestParam String email
    ) {

        return ResponseEntity.ok(
                adminSearchService
                        .findCustomerByEmail(email)
        );
    }

    @GetMapping("/tickets")
    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER','SUPPORT_AGENT','SCANNER_STAFF','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<AdminTicketLookupResponse>
    findTicketByNumber(
            @RequestParam String ticketNumber
    ) {

        return ResponseEntity.ok(
                adminSearchService
                        .findTicketByNumber(ticketNumber)
        );
    }
}
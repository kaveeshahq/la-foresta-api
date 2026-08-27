package com.laforesta.api.order.controller;

import com.laforesta.api.order.dto.AdminOrderResponse;
import com.laforesta.api.order.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping("/{orderId}")
    @PreAuthorize(
            "hasAnyRole('FINANCE_MANAGER','SUPPORT_AGENT','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<AdminOrderResponse> getOrder(
            @PathVariable UUID orderId
    ) {

        return ResponseEntity.ok(
                adminOrderService.getOrder(orderId)
        );
    }
}
package com.laforesta.api.refund.controller;

import com.laforesta.api.refund.dto.CreateRefundRequest;
import com.laforesta.api.refund.dto.RefundResponse;
import com.laforesta.api.refund.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class MockRefundController {

    private final RefundService refundService;

    @PostMapping("/{orderId}/refund")
    @PreAuthorize(
            "hasAnyRole('FINANCE_MANAGER','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<RefundResponse> refundOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateRefundRequest request
    ) {

        return ResponseEntity.ok(
                refundService.refundOrder(
                        orderId,
                        request.reason()
                )
        );
    }

    @GetMapping("/{orderId}/refunds")
    @PreAuthorize(
            "hasAnyRole('FINANCE_MANAGER','ADMIN','SUPER_ADMIN')"
    )
    public ResponseEntity<List<RefundResponse>> getRefunds(
            @PathVariable UUID orderId
    ) {

        return ResponseEntity.ok(
                refundService.getRefundsForOrder(
                        orderId
                )
        );
    }
}
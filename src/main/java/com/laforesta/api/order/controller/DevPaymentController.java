package com.laforesta.api.order.controller;

import com.laforesta.api.order.service.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dev/payments")
@RequiredArgsConstructor
public class DevPaymentController {

    private final OrderPaymentService orderPaymentService;

    @PostMapping("/{orderId}/success")
    public ResponseEntity<Void> success(
            @PathVariable UUID orderId
    ) {

        orderPaymentService
                .confirmPayment(orderId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/{orderId}/failure")
    public ResponseEntity<Void> failure(
            @PathVariable UUID orderId
    ) {

        orderPaymentService
                .failPayment(orderId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
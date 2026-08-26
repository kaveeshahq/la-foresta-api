package com.laforesta.api.payment.controller;

import com.laforesta.api.payment.dto.PaymentResponse;
import com.laforesta.api.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{orderId}/payments")
    public ResponseEntity<List<PaymentResponse>> getOrderPayments(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId
    ) {

        UUID userId =
                UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                paymentService.getPaymentsForOrder(
                        userId,
                        orderId
                )
        );
    }
}
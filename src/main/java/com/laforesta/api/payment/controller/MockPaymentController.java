package com.laforesta.api.payment.controller;

import com.laforesta.api.payment.dto.InitiatePaymentRequest;
import com.laforesta.api.payment.dto.PaymentResponse;
import com.laforesta.api.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments/mock")
@RequiredArgsConstructor
public class MockPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiate(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InitiatePaymentRequest request
    ) {

        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        PaymentResponse response =
                paymentService.initiateMockPayment(
                        userId,
                        request.orderId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{paymentId}/success")
    public ResponseEntity<PaymentResponse> success(
            @PathVariable UUID paymentId
    ) {

        return ResponseEntity.ok(
                paymentService
                        .completeMockPayment(
                                paymentId
                        )
        );
    }

    @PostMapping("/{paymentId}/failure")
    public ResponseEntity<PaymentResponse> failure(
            @PathVariable UUID paymentId
    ) {

        return ResponseEntity.ok(
                paymentService
                        .failMockPayment(
                                paymentId
                        )
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID paymentId
    ) {

        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                paymentService
                        .getPaymentForUser(
                                userId,
                                paymentId
                        )
        );
    }
}
package com.laforesta.api.payment.controller;

import com.laforesta.api.payment.dto.InitiatePaymentRequest;
import com.laforesta.api.payment.dto.PaymentResponse;
import com.laforesta.api.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/guest/payments/mock")
@RequiredArgsConstructor
public class GuestMockPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiate(
            @Valid @RequestBody InitiatePaymentRequest request
    ) {

        PaymentResponse response =
                paymentService
                        .initiateGuestMockPayment(
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
            @PathVariable UUID paymentId
    ) {

        return ResponseEntity.ok(
                paymentService
                        .getGuestPayment(
                                paymentId
                        )
        );
    }
}
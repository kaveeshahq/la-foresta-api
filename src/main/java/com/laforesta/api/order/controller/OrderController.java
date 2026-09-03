package com.laforesta.api.order.controller;

import com.laforesta.api.order.dto.CreateOrderRequest;
import com.laforesta.api.order.dto.OrderResponse;
import com.laforesta.api.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrderRequest request
    ) {

        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        OrderResponse response =
                orderService.createOrder(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {

        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                orderService.getOrder(
                        userId,
                        id
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                orderService.getOrders(
                        userId
                )
        );
    }
}
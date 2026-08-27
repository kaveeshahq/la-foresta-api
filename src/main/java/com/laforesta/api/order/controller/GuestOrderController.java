package com.laforesta.api.order.controller;

import com.laforesta.api.order.dto.CreateOrderRequest;

import com.laforesta.api.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.laforesta.api.order.dto.GuestOrderResponse;

@RestController
@RequestMapping("/api/guest/orders")
@RequiredArgsConstructor
public class GuestOrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<GuestOrderResponse> createGuestOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {

        GuestOrderResponse response =
                orderService.createGuestOrder(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
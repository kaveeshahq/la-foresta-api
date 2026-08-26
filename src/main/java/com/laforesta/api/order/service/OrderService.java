package com.laforesta.api.order.service;

import com.laforesta.api.order.dto.CreateOrderRequest;
import com.laforesta.api.order.dto.OrderItemResponse;
import com.laforesta.api.order.dto.OrderResponse;
import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.entity.OrderItem;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.ticket.entity.TicketReservation;
import com.laforesta.api.ticket.entity.TicketReservationItem;
import com.laforesta.api.ticket.model.ReservationStatus;
import com.laforesta.api.ticket.repository.TicketReservationRepository;
import com.laforesta.api.user.entity.User;
import com.laforesta.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TicketReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(
            UUID userId,
            CreateOrderRequest request
    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        TicketReservation reservation =
                reservationRepository
                        .findById(request.reservationId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Reservation not found"
                                )
                        );

        if (reservation.getUser() == null
                || !reservation.getUser()
                .getId()
                .equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Reservation does not belong to this user"
            );
        }

        if (reservation.getStatus()
                != ReservationStatus.ACTIVE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation is not active"
            );
        }

        if (!reservation.getExpiresAt()
                .isAfter(OffsetDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation has expired"
            );
        }

        if (orderRepository.existsByReservation(
                reservation
        )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An order already exists for this reservation"
            );
        }

        if (reservation.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation contains no items"
            );
        }

        Order order = new Order();

        order.setUser(user);
        order.setReservation(reservation);
        order.setStatus(
                OrderStatus.PENDING_PAYMENT
        );

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        String orderCurrency = null;

        List<OrderItemResponse> responseItems =
                new ArrayList<>();

        for (TicketReservationItem reservationItem
                : reservation.getItems()) {

            if (orderCurrency == null) {
                orderCurrency =
                        reservationItem.getCurrency();

            } else if (!orderCurrency.equals(
                    reservationItem.getCurrency()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Reservation contains multiple currencies"
                );
            }

            BigDecimal lineTotal =
                    reservationItem
                            .getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            reservationItem.getQuantity()
                                    )
                            );

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setTicketType(
                    reservationItem.getTicketType()
            );

            orderItem.setTicketTypeName(
                    reservationItem
                            .getTicketType()
                            .getName()
            );

            orderItem.setQuantity(
                    reservationItem.getQuantity()
            );

            orderItem.setUnitPrice(
                    reservationItem.getUnitPrice()
            );

            orderItem.setCurrency(
                    reservationItem.getCurrency()
            );

            orderItem.setLineTotal(
                    lineTotal
            );

            order.addItem(orderItem);

            totalAmount =
                    totalAmount.add(lineTotal);

            responseItems.add(
                    new OrderItemResponse(
                            reservationItem
                                    .getTicketType()
                                    .getId(),
                            reservationItem
                                    .getTicketType()
                                    .getName(),
                            reservationItem
                                    .getQuantity(),
                            reservationItem
                                    .getUnitPrice(),
                            reservationItem
                                    .getCurrency(),
                            lineTotal
                    )
            );
        }

        order.setTotalAmount(totalAmount);
        order.setCurrency(orderCurrency);

        Order savedOrder =
                orderRepository.save(order);

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder
                        .getReservation()
                        .getId(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount(),
                savedOrder.getCurrency(),
                responseItems,
                savedOrder.getCreatedAt(),
                savedOrder.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(
            UUID userId,
            UUID orderId
    ) {

        Order order = orderRepository
                .findByIdAndUserId(
                        orderId,
                        userId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderItemResponse(
                                        item.getTicketType()
                                                .getId(),
                                        item.getTicketTypeName(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getCurrency(),
                                        item.getLineTotal()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getReservation().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
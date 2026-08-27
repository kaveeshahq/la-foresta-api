package com.laforesta.api.order.repository;

import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.ticket.entity.TicketReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository
        extends JpaRepository<Order, UUID> {

    boolean existsByReservation(
            TicketReservation reservation
    );

    Optional<Order> findByReservation(
            TicketReservation reservation
    );

    Optional<Order> findByIdAndUserId(
            UUID orderId,
            UUID userId
    );

    long countByPromoCodeIdAndStatus(
            UUID promoCodeId,
            OrderStatus status
    );

    long countByPromoCodeIdAndUserIdAndStatus(
            UUID promoCodeId,
            UUID userId,
            OrderStatus status
    );
}
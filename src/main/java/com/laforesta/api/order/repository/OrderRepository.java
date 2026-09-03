package com.laforesta.api.order.repository;

import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.ticket.entity.TicketReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository
        extends JpaRepository<Order, UUID> {

    Optional<Order> findByGuestAccessTokenHash(
            String guestAccessTokenHash
    );

    Optional<Order> findByGuestEmailAccessTokenHash(
            String guestEmailAccessTokenHash
    );

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

    @EntityGraph(attributePaths = {
            "reservation",
            "items",
            "items.ticketType"
    })
    List<Order> findAllByUserIdOrderByCreatedAtDesc(
            UUID userId
    );

    List<Order> findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(
            String email
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
package com.laforesta.api.order.service;

import com.laforesta.api.order.dto.CreateOrderRequest;
import com.laforesta.api.order.dto.OrderItemResponse;
import com.laforesta.api.order.dto.OrderResponse;
import com.laforesta.api.order.entity.Order;
import com.laforesta.api.order.entity.OrderItem;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.promo.dto.PromoCalculationResult;
import com.laforesta.api.promo.entity.PromoCode;
import com.laforesta.api.promo.repository.PromoCodeRepository;
import com.laforesta.api.promo.service.PromoCodeService;
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
import java.math.RoundingMode;
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

    private final PromoCodeService promoCodeService;
    private final PromoCodeRepository promoCodeRepository;

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

        BigDecimal subtotalAmount =
                BigDecimal.ZERO;

        String orderCurrency = null;

        UUID eventId = null;

        List<OrderItemResponse> responseItems =
                new ArrayList<>();

        for (TicketReservationItem reservationItem
                : reservation.getItems()) {

            /*
             * Currency consistency
             */
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

            /*
             * Event consistency
             */
            UUID itemEventId =
                    reservationItem
                            .getTicketType()
                            .getEvent()
                            .getId();

            if (eventId == null) {

                eventId = itemEventId;

            } else if (!eventId.equals(
                    itemEventId
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Reservation contains tickets from multiple events"
                );
            }

            BigDecimal lineTotal =
                    reservationItem
                            .getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            reservationItem
                                                    .getQuantity()
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
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

            subtotalAmount =
                    subtotalAmount.add(lineTotal);

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

        subtotalAmount =
                subtotalAmount.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal discountAmount =
                BigDecimal.ZERO.setScale(2);

        BigDecimal totalAmount =
                subtotalAmount;

        /*
         * Optional promo calculation
         */
        if (request.promoCode() != null
                && !request.promoCode().isBlank()) {

            PromoCalculationResult promoResult =
                    promoCodeService
                            .validateAndCalculate(
                                    request.promoCode(),
                                    eventId,
                                    subtotalAmount
                            );

            PromoCode promoCode =
                    promoCodeRepository
                            .findById(
                                    promoResult
                                            .promoCodeId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Promo code not found"
                                    )
                            );

            discountAmount =
                    promoResult.discountAmount();

            totalAmount =
                    promoResult.finalAmount();

            order.setPromoCode(
                    promoCode
            );

            /*
             * Snapshot the code used at checkout.
             * Later edits to the PromoCode entity will not
             * change what code appeared on this order.
             */
            order.setPromoCodeSnapshot(
                    promoResult.code()
            );
        }

        order.setSubtotalAmount(
                subtotalAmount
        );

        order.setDiscountAmount(
                discountAmount
        );

        order.setTotalAmount(
                totalAmount
        );

        order.setCurrency(
                orderCurrency
        );

        Order savedOrder =
                orderRepository.save(order);

        return toResponse(
                savedOrder,
                responseItems
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

        return toResponse(
                order,
                items
        );
    }

    private OrderResponse toResponse(
            Order order,
            List<OrderItemResponse> items
    ) {

        return new OrderResponse(
                order.getId(),

                order.getReservation()
                        .getId(),

                order.getStatus(),

                order.getSubtotalAmount(),

                order.getDiscountAmount(),

                order.getTotalAmount(),

                order.getPromoCodeSnapshot(),

                order.getCurrency(),

                items,

                order.getCreatedAt(),

                order.getUpdatedAt()
        );
    }
}
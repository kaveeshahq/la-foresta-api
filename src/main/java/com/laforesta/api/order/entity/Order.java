package com.laforesta.api.order.entity;

import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.promo.entity.PromoCode;
import com.laforesta.api.ticket.entity.TicketReservation;
import com.laforesta.api.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(
            name = "guest_email",
            length = 255
    )
    private String guestEmail;

    @Column(
            name = "guest_name",
            length = 150
    )
    private String guestName;

    @Column(
            name = "guest_access_token_hash",
            length = 64
    )
    private String guestAccessTokenHash;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reservation_id",
            nullable = false,
            unique = true
    )
    private TicketReservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private OrderStatus status;

    @Column(
            name = "subtotal_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotalAmount;

    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountAmount;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    @Column(
            name = "promo_code_snapshot",
            length = 50
    )
    private String promoCodeSnapshot;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items =
            new ArrayList<>();

    @PrePersist
    public void onCreate() {

        OffsetDateTime now =
                OffsetDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.discountAmount == null) {
            this.discountAmount =
                    BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void onUpdate() {

        this.updatedAt =
                OffsetDateTime.now();
    }

    public void addItem(
            OrderItem item
    ) {

        items.add(item);
        item.setOrder(this);
    }
}
package com.laforesta.api.promo.entity;

import com.laforesta.api.event.entity.Event;
import com.laforesta.api.promo.model.DiscountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "promo_codes")
@Getter
@Setter
@NoArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "discount_type",
            nullable = false,
            length = 30
    )
    private DiscountType discountType;

    @Column(
            name = "discount_value",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountValue;

    @Column(
            name = "minimum_order_amount",
            precision = 12,
            scale = 2
    )
    private BigDecimal minimumOrderAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(name = "valid_from")
    private OffsetDateTime validFrom;

    @Column(name = "valid_until")
    private OffsetDateTime validUntil;

    @Column(nullable = false)
    private boolean active;

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

    @PrePersist
    public void onCreate() {

        OffsetDateTime now =
                OffsetDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
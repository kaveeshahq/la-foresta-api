package com.laforesta.api.ticket.entity;

import com.laforesta.api.ticket.model.ReservationStatus;
import com.laforesta.api.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ticket_reservations")
@Getter
@Setter
@NoArgsConstructor
public class TicketReservation {

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

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private ReservationStatus status;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private OffsetDateTime expiresAt;

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
            mappedBy = "reservation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TicketReservationItem> items =
            new ArrayList<>();

    @PrePersist
    public void onCreate() {

        OffsetDateTime now =
                OffsetDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {

        this.updatedAt =
                OffsetDateTime.now();
    }

    public void addItem(
            TicketReservationItem item
    ) {

        items.add(item);
        item.setReservation(this);
    }
}
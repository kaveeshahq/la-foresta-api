ALTER TABLE ticket_reservations
    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE orders
    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE orders
    ADD COLUMN guest_email VARCHAR(255),
    ADD COLUMN guest_name VARCHAR(150);

ALTER TABLE ticket_reservations
    ADD COLUMN guest_email VARCHAR(255),
    ADD COLUMN guest_name VARCHAR(150);

ALTER TABLE orders
    ADD CONSTRAINT chk_order_customer_identity
        CHECK (
            user_id IS NOT NULL
                OR (
                guest_email IS NOT NULL
                    AND guest_name IS NOT NULL
                )
            );

ALTER TABLE ticket_reservations
    ADD CONSTRAINT chk_reservation_customer_identity
        CHECK (
            user_id IS NOT NULL
                OR (
                guest_email IS NOT NULL
                    AND guest_name IS NOT NULL
                )
            );

CREATE INDEX idx_orders_guest_email
    ON orders(guest_email);

CREATE INDEX idx_ticket_reservations_guest_email
    ON ticket_reservations(guest_email);
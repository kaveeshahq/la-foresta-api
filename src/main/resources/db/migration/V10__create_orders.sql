CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                        user_id UUID NOT NULL,

                        reservation_id UUID NOT NULL UNIQUE,

                        status VARCHAR(30) NOT NULL,

                        total_amount NUMERIC(12,2) NOT NULL,

                        currency VARCHAR(3) NOT NULL,

                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        CONSTRAINT fk_orders_user
                            FOREIGN KEY (user_id)
                                REFERENCES users(id)
                                ON DELETE RESTRICT,

                        CONSTRAINT fk_orders_reservation
                            FOREIGN KEY (reservation_id)
                                REFERENCES ticket_reservations(id)
                                ON DELETE RESTRICT,

                        CONSTRAINT chk_orders_total_amount
                            CHECK (total_amount >= 0)
);

CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             order_id UUID NOT NULL,

                             ticket_type_id UUID NOT NULL,

                             ticket_type_name VARCHAR(150) NOT NULL,

                             quantity INTEGER NOT NULL,

                             unit_price NUMERIC(12,2) NOT NULL,

                             currency VARCHAR(3) NOT NULL,

                             line_total NUMERIC(12,2) NOT NULL,

                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_order_items_ticket_type
                                 FOREIGN KEY (ticket_type_id)
                                     REFERENCES ticket_types(id)
                                     ON DELETE RESTRICT,

                             CONSTRAINT chk_order_items_quantity
                                 CHECK (quantity > 0),

                             CONSTRAINT chk_order_items_unit_price
                                 CHECK (unit_price >= 0),

                             CONSTRAINT chk_order_items_line_total
                                 CHECK (line_total >= 0)
);

CREATE INDEX idx_orders_user_id
    ON orders(user_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);

CREATE INDEX idx_order_items_ticket_type_id
    ON order_items(ticket_type_id);
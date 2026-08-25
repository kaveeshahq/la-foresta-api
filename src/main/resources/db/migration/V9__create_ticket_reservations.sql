CREATE TABLE ticket_reservations (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                     user_id UUID,

                                     status VARCHAR(30) NOT NULL,

                                     expires_at TIMESTAMPTZ NOT NULL,

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                     CONSTRAINT fk_ticket_reservations_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES users(id)
                                             ON DELETE SET NULL
);

CREATE TABLE ticket_reservation_items (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                          reservation_id UUID NOT NULL,

                                          ticket_type_id UUID NOT NULL,

                                          quantity INTEGER NOT NULL,

                                          unit_price NUMERIC(12,2) NOT NULL,

                                          currency VARCHAR(3) NOT NULL,

                                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                          CONSTRAINT fk_reservation_items_reservation
                                              FOREIGN KEY (reservation_id)
                                                  REFERENCES ticket_reservations(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_reservation_items_ticket_type
                                              FOREIGN KEY (ticket_type_id)
                                                  REFERENCES ticket_types(id)
                                                  ON DELETE RESTRICT,

                                          CONSTRAINT chk_reservation_item_quantity
                                              CHECK (quantity > 0),

                                          CONSTRAINT chk_reservation_item_price
                                              CHECK (unit_price >= 0),

                                          CONSTRAINT uq_reservation_ticket_type
                                              UNIQUE (reservation_id, ticket_type_id)
);

CREATE INDEX idx_ticket_reservations_user_id
    ON ticket_reservations(user_id);

CREATE INDEX idx_ticket_reservations_status
    ON ticket_reservations(status);

CREATE INDEX idx_ticket_reservations_expires_at
    ON ticket_reservations(expires_at);

CREATE INDEX idx_reservation_items_reservation_id
    ON ticket_reservation_items(reservation_id);

CREATE INDEX idx_reservation_items_ticket_type_id
    ON ticket_reservation_items(ticket_type_id);
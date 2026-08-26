CREATE TABLE tickets (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         order_id UUID NOT NULL,

                         ticket_type_id UUID NOT NULL,

                         user_id UUID,

                         ticket_number VARCHAR(50) NOT NULL UNIQUE,

                         qr_token VARCHAR(255) NOT NULL UNIQUE,

                         status VARCHAR(30) NOT NULL,

                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                         updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                         CONSTRAINT fk_tickets_order
                             FOREIGN KEY (order_id)
                                 REFERENCES orders(id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT fk_tickets_ticket_type
                             FOREIGN KEY (ticket_type_id)
                                 REFERENCES ticket_types(id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT fk_tickets_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
                                 ON DELETE SET NULL
);

CREATE INDEX idx_tickets_order_id
    ON tickets(order_id);

CREATE INDEX idx_tickets_ticket_type_id
    ON tickets(ticket_type_id);

CREATE INDEX idx_tickets_user_id
    ON tickets(user_id);

CREATE INDEX idx_tickets_status
    ON tickets(status);

CREATE INDEX idx_tickets_qr_token
    ON tickets(qr_token);
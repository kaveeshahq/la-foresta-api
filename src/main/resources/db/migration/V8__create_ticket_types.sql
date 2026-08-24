CREATE TABLE ticket_types (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                              event_id UUID NOT NULL,

                              name VARCHAR(150) NOT NULL,

                              description VARCHAR(500),

                              price NUMERIC(12,2) NOT NULL,

                              currency VARCHAR(3) NOT NULL DEFAULT 'LKR',

                              capacity INTEGER NOT NULL,

                              max_per_order INTEGER NOT NULL DEFAULT 10,

                              sales_start_at TIMESTAMPTZ,

                              sales_end_at TIMESTAMPTZ,

                              active BOOLEAN NOT NULL DEFAULT TRUE,

                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                              updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                              CONSTRAINT fk_ticket_types_event
                                  FOREIGN KEY (event_id)
                                      REFERENCES events(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT chk_ticket_type_price
                                  CHECK (price >= 0),

                              CONSTRAINT chk_ticket_type_capacity
                                  CHECK (capacity > 0),

                              CONSTRAINT chk_ticket_type_max_per_order
                                  CHECK (max_per_order > 0),

                              CONSTRAINT chk_ticket_type_sales_window
                                  CHECK (
                                      sales_start_at IS NULL
                                          OR sales_end_at IS NULL
                                          OR sales_end_at > sales_start_at
                                      ),

                              CONSTRAINT uq_ticket_type_event_name
                                  UNIQUE (event_id, name)
);

CREATE INDEX idx_ticket_types_event_id
    ON ticket_types(event_id);

CREATE INDEX idx_ticket_types_active
    ON ticket_types(active);
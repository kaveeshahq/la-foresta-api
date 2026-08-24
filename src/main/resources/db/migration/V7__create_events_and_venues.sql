CREATE TABLE venues (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name VARCHAR(150) NOT NULL,
                        address_line_1 VARCHAR(255),
                        address_line_2 VARCHAR(255),
                        city VARCHAR(100),
                        country VARCHAR(100) NOT NULL DEFAULT 'Sri Lanka',
                        latitude NUMERIC(9,6),
                        longitude NUMERIC(9,6),
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE events (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                        venue_id UUID,

                        title VARCHAR(200) NOT NULL,
                        slug VARCHAR(220) NOT NULL UNIQUE,

                        short_description VARCHAR(500),
                        description TEXT,

                        status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

                        starts_at TIMESTAMPTZ NOT NULL,
                        ends_at TIMESTAMPTZ,

                        sales_start_at TIMESTAMPTZ,
                        sales_end_at TIMESTAMPTZ,

                        minimum_age INTEGER NOT NULL DEFAULT 18,

                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        CONSTRAINT fk_events_venue
                            FOREIGN KEY (venue_id)
                                REFERENCES venues(id)
                                ON DELETE SET NULL,

                        CONSTRAINT chk_event_time
                            CHECK (ends_at IS NULL OR ends_at > starts_at),

                        CONSTRAINT chk_event_age
                            CHECK (minimum_age >= 0)
);

CREATE INDEX idx_events_status
    ON events(status);

CREATE INDEX idx_events_starts_at
    ON events(starts_at);

CREATE INDEX idx_events_venue_id
    ON events(venue_id);
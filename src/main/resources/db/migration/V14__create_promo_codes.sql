CREATE TABLE promo_codes (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             event_id UUID,

                             code VARCHAR(50) NOT NULL UNIQUE,

                             discount_type VARCHAR(30) NOT NULL,

                             discount_value NUMERIC(12,2) NOT NULL,

                             minimum_order_amount NUMERIC(12,2),

                             usage_limit INTEGER,

                             per_user_limit INTEGER,

                             valid_from TIMESTAMPTZ,

                             valid_until TIMESTAMPTZ,

                             active BOOLEAN NOT NULL DEFAULT TRUE,

                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                             updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                             CONSTRAINT fk_promo_codes_event
                                 FOREIGN KEY (event_id)
                                     REFERENCES events(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT chk_promo_discount_value
                                 CHECK (discount_value > 0),

                             CONSTRAINT chk_promo_minimum_order_amount
                                 CHECK (
                                     minimum_order_amount IS NULL
                                         OR minimum_order_amount >= 0
                                     ),

                             CONSTRAINT chk_promo_usage_limit
                                 CHECK (
                                     usage_limit IS NULL
                                         OR usage_limit > 0
                                     ),

                             CONSTRAINT chk_promo_per_user_limit
                                 CHECK (
                                     per_user_limit IS NULL
                                         OR per_user_limit > 0
                                     ),

                             CONSTRAINT chk_promo_valid_dates
                                 CHECK (
                                     valid_until IS NULL
                                         OR valid_from IS NULL
                                         OR valid_until > valid_from
                                     )
);

CREATE INDEX idx_promo_codes_event_id
    ON promo_codes(event_id);

CREATE INDEX idx_promo_codes_active
    ON promo_codes(active);

CREATE INDEX idx_promo_codes_valid_until
    ON promo_codes(valid_until);
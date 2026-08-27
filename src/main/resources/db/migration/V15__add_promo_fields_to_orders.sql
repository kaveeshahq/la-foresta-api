ALTER TABLE orders
    ADD COLUMN subtotal_amount NUMERIC(12,2),

    ADD COLUMN discount_amount NUMERIC(12,2)
        NOT NULL DEFAULT 0,

    ADD COLUMN promo_code_id UUID,

    ADD COLUMN promo_code_snapshot VARCHAR(50);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_promo_code
        FOREIGN KEY (promo_code_id)
            REFERENCES promo_codes(id)
            ON DELETE SET NULL;

UPDATE orders
SET subtotal_amount = total_amount
WHERE subtotal_amount IS NULL;

ALTER TABLE orders
    ALTER COLUMN subtotal_amount SET NOT NULL;

CREATE INDEX idx_orders_promo_code_id
    ON orders(promo_code_id);
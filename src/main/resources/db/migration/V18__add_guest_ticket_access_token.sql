ALTER TABLE orders
    ADD COLUMN guest_access_token_hash VARCHAR(64);

CREATE UNIQUE INDEX uq_orders_guest_access_token_hash
    ON orders(guest_access_token_hash)
    WHERE guest_access_token_hash IS NOT NULL;
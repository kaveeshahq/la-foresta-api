CREATE TABLE payment_transactions (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                      order_id UUID NOT NULL,

                                      provider VARCHAR(30) NOT NULL,

                                      status VARCHAR(30) NOT NULL,

                                      provider_reference VARCHAR(255),

                                      amount NUMERIC(12,2) NOT NULL,

                                      currency VARCHAR(3) NOT NULL,

                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                      CONSTRAINT fk_payment_transactions_order
                                          FOREIGN KEY (order_id)
                                              REFERENCES orders(id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT chk_payment_transactions_amount
                                          CHECK (amount >= 0)
);

CREATE INDEX idx_payment_transactions_order_id
    ON payment_transactions(order_id);

CREATE INDEX idx_payment_transactions_status
    ON payment_transactions(status);

CREATE INDEX idx_payment_transactions_provider_reference
    ON payment_transactions(provider_reference);
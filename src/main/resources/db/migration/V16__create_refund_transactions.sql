CREATE TABLE refund_transactions (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                     order_id UUID NOT NULL,

                                     payment_transaction_id UUID,

                                     provider VARCHAR(30) NOT NULL,

                                     status VARCHAR(30) NOT NULL,

                                     amount NUMERIC(12,2) NOT NULL,

                                     currency VARCHAR(3) NOT NULL,

                                     reason VARCHAR(255),

                                     provider_reference VARCHAR(255),

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                     CONSTRAINT fk_refund_order
                                         FOREIGN KEY (order_id)
                                             REFERENCES orders(id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT fk_refund_payment
                                         FOREIGN KEY (payment_transaction_id)
                                             REFERENCES payment_transactions(id)
                                             ON DELETE SET NULL,

                                     CONSTRAINT chk_refund_amount
                                         CHECK (amount > 0)
);

CREATE INDEX idx_refund_transactions_order_id
    ON refund_transactions(order_id);

CREATE INDEX idx_refund_transactions_payment_id
    ON refund_transactions(payment_transaction_id);

CREATE INDEX idx_refund_transactions_status
    ON refund_transactions(status);
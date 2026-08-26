CREATE TABLE check_ins (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           ticket_id UUID NOT NULL UNIQUE,

                           scanned_by_user_id UUID,

                           checked_in_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_check_ins_ticket
                               FOREIGN KEY (ticket_id)
                                   REFERENCES tickets(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_check_ins_scanned_by_user
                               FOREIGN KEY (scanned_by_user_id)
                                   REFERENCES users(id)
                                   ON DELETE SET NULL
);

CREATE INDEX idx_check_ins_scanned_by_user_id
    ON check_ins(scanned_by_user_id);

CREATE INDEX idx_check_ins_checked_in_at
    ON check_ins(checked_in_at);
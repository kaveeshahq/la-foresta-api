CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(50) NOT NULL UNIQUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       full_name VARCHAR(150) NOT NULL,
                       password_hash VARCHAR(255),
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,
                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
                                    ON DELETE CASCADE
);

INSERT INTO roles (name)
VALUES
    ('CUSTOMER'),
    ('CONTENT_EDITOR'),
    ('EVENT_MANAGER'),
    ('FINANCE_MANAGER'),
    ('SUPPORT_AGENT'),
    ('SCANNER_STAFF'),
    ('ADMIN'),
    ('SUPER_ADMIN');
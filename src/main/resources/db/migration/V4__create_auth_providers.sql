CREATE TABLE auth_providers (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                user_id UUID NOT NULL,

                                provider VARCHAR(30) NOT NULL,

                                provider_user_id VARCHAR(255) NOT NULL,

                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                CONSTRAINT fk_auth_providers_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT uq_auth_provider_identity
                                    UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_auth_providers_user_id
    ON auth_providers(user_id);
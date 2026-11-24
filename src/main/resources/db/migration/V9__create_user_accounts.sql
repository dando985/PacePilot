CREATE TABLE user_accounts
(
    id             UUID PRIMARY KEY,
    email          VARCHAR(254) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT user_accounts_email_not_blank
        CHECK (char_length(btrim(email)) > 0),

    CONSTRAINT user_accounts_email_trimmed
        CHECK (email = btrim(email)),

    CONSTRAINT user_accounts_password_hash_not_blank
        CHECK (char_length(btrim(password_hash)) > 0)
);

CREATE UNIQUE INDEX user_accounts_email_unique
    ON user_accounts (lower(email));
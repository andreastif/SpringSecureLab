ALTER TABLE confirmation_tokens
    RENAME COLUMN createdat TO created_at;

ALTER TABLE confirmation_tokens
    RENAME COLUMN expiresAt TO expires_at;

ALTER TABLE confirmation_tokens
    RENAME COLUMN confirmedat TO confirmed_at;
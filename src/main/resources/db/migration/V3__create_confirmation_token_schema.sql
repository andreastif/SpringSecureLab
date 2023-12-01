CREATE TABLE confirmation_tokens (
    id BIGSERIAL NOT NULL,
    token UUID UNIQUE NOT NULL,
    createdAt TIMESTAMP WITH TIME ZONE NOT NULL,
    expiresAt TIMESTAMP WITH TIME ZONE NOT NULL,
    confirmedAt TIMESTAMP WITH TIME ZONE,
    member_id BIGSERIAL NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES members(id)
)
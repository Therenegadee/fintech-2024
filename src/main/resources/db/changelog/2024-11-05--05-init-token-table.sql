--liquibase formatted sql

--changeset melkinda:init-token-table
CREATE TABLE users.token (
    token varchar not null unique,
    black_listed boolean not null
);
--rollback DROP TABLE users.token CASCADE;

--changeset melkinda:create-hash-index
CREATE INDEX token_hash_idx ON users.token USING hash (token);
--rollback DROP INDEX token_hash_idx;
--liquibase formatted sql

--changeset melkinda:init-user-table
CREATE TABLE users.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at DATE DEFAULT CURRENT_DATE NOT NULL,
    updated_at DATE DEFAULT CURRENT_DATE NOT NULL
);
--rollback DROP TABLE users.users CASCADE;
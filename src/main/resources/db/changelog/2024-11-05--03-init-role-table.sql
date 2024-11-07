--liquibase formatted sql

--changeset melkinda:init-roles-table
CREATE TABLE users.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_at DATE DEFAULT CURRENT_DATE NOT NULL,
    updated_at DATE DEFAULT CURRENT_DATE NOT NULL
);
--rollback DROP TABLE users.roles CASCADE;
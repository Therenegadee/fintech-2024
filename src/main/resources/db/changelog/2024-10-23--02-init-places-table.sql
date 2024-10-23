--liquibase formatted sql

--changeset melkinda:init-places-table
CREATE TABLE IF NOT EXISTS service.places (
    id serial primary key,
    slug varchar(250) not null,
    name varchar(300) not null
)
--rollback DROP TABLE service.places CASCADE;
--liquibase formatted sql

--changeset melkinda:init-events-table
CREATE TABLE IF NOT EXISTS service.events (
    id serial primary key,
    name varchar(300) not null,
    date_from timestamptz not null,
    date_to timestamptz not null,
    price DOUBLE PRECISION not null,
    place_id integer not null,
    FOREIGN KEY (place_id) REFERENCES service.places (id)
)
--rollback DROP TABLE service.events CASCADE;
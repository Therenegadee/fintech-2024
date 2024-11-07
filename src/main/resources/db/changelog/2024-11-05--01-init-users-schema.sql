--liquibase formatted sql

--changeset melkinda:init-users-schema
CREATE SCHEMA IF NOT EXISTS users;
--rollback DROP SCHEMA users CASCADE;
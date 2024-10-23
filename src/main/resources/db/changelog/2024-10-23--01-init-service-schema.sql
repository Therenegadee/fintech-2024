--liquibase formatted sql

--changeset melkinda:init-service-schema
CREATE SCHEMA IF NOT EXISTS service;
--rollback DROP SCHEMA service CASCADE;
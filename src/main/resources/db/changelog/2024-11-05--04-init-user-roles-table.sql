--liquibase formatted sql

--changeset melkinda:init-user-roles-table
CREATE TABLE users.user_roles (
    user_id BIGINT NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users.users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES users.roles(id) ON DELETE CASCADE
);
--rollback DROP TABLE users.user_roles CASCADE;
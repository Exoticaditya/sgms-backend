SET search_path TO public;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------- common trigger ----------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------- roles ----------
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TRIGGER trg_roles_updated_at
BEFORE UPDATE ON roles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- users ----------
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','LOCKED')),
    CONSTRAINT chk_users_deleted_active CHECK (deleted_at IS NULL OR active = false)
);

-- case-insensitive login
CREATE UNIQUE INDEX uk_users_email_lower
ON users (LOWER(email))
WHERE deleted_at IS NULL;

CREATE INDEX idx_users_login_lookup
ON users (LOWER(email), deleted_at);

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- user_roles ----------
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uq_user_roles_active
ON user_roles(user_id, role_id)
WHERE active = true;

CREATE TRIGGER trg_user_roles_updated_at
BEFORE UPDATE ON user_roles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

INSERT INTO roles (name) VALUES
('ADMIN'),
('SUPERVISOR'),
('GUARD'),
('CLIENT')
ON CONFLICT DO NOTHING;

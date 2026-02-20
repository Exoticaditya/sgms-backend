SET search_path TO public;

CREATE TABLE guards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    supervisor_user_id BIGINT,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(30),
    hire_date DATE,
    base_salary NUMERIC(12,2) DEFAULT 0,
    per_day_rate NUMERIC(12,2) DEFAULT 0,
    overtime_rate NUMERIC(12,2) DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_guard_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_guard_supervisor
        FOREIGN KEY (supervisor_user_id) REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT chk_guard_deleted_active CHECK (deleted_at IS NULL OR active = false)
);

CREATE INDEX idx_guards_supervisor ON guards(supervisor_user_id);

CREATE TRIGGER trg_guards_updated_at
BEFORE UPDATE ON guards
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

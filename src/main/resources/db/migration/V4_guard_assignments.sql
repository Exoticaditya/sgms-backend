SET search_path TO public;

CREATE TABLE shift_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

CREATE TABLE guard_assignments (
    id BIGSERIAL PRIMARY KEY,
    guard_id BIGINT,
    site_post_id BIGINT NOT NULL,
    shift_type_id BIGINT NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_user_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_assignment_guard
        FOREIGN KEY (guard_id) REFERENCES guards(id) ON DELETE SET NULL,

    CONSTRAINT fk_assignment_post
        FOREIGN KEY (site_post_id) REFERENCES site_posts(id) ON DELETE RESTRICT,

    CONSTRAINT fk_assignment_shift
        FOREIGN KEY (shift_type_id) REFERENCES shift_types(id) ON DELETE RESTRICT,

    CONSTRAINT fk_assignment_creator
        FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT chk_assignment_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_assignment_guard ON guard_assignments(guard_id);
CREATE INDEX idx_assignment_post ON guard_assignments(site_post_id);

CREATE TRIGGER trg_assignments_updated_at
BEFORE UPDATE ON guard_assignments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

INSERT INTO shift_types(name,start_time,end_time) VALUES
('DAY','06:00','14:00'),
('EVENING','14:00','22:00'),
('NIGHT','22:00','06:00')
ON CONFLICT DO NOTHING;







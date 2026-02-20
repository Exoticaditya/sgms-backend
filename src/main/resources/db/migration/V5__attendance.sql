SET search_path TO public;

CREATE TABLE attendance_logs (
    id BIGSERIAL PRIMARY KEY,
    guard_id BIGINT,
    assignment_id BIGINT,
    attendance_date DATE NOT NULL,
    check_in_time TIMESTAMPTZ,
    check_out_time TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    late_minutes INT DEFAULT 0,
    early_leave_minutes INT DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_att_guard
        FOREIGN KEY (guard_id) REFERENCES guards(id) ON DELETE SET NULL,

    CONSTRAINT fk_att_assignment
        FOREIGN KEY (assignment_id) REFERENCES guard_assignments(id) ON DELETE SET NULL,

    CONSTRAINT uq_attendance UNIQUE(assignment_id, attendance_date)
);

CREATE INDEX idx_attendance_date ON attendance_logs(attendance_date);

CREATE TRIGGER trg_attendance_updated_at
BEFORE UPDATE ON attendance_logs
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

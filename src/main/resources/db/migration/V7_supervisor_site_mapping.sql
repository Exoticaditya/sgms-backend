SET search_path TO public;

CREATE TABLE IF NOT EXISTS supervisor_site_mapping (
    id BIGSERIAL PRIMARY KEY,
    supervisor_user_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    removed_at TIMESTAMPTZ,

    CONSTRAINT fk_supervisor_site_mapping_supervisor
        FOREIGN KEY (supervisor_user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_supervisor_site_mapping_site
        FOREIGN KEY (site_id)
        REFERENCES sites(id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_supervisor_site_mapping_supervisor
ON supervisor_site_mapping(supervisor_user_id);

CREATE INDEX IF NOT EXISTS idx_supervisor_site_mapping_site
ON supervisor_site_mapping(site_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_supervisor_site_mapping_active
ON supervisor_site_mapping(supervisor_user_id, site_id)
WHERE removed_at IS NULL;

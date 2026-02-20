SET search_path TO public;

CREATE TABLE IF NOT EXISTS client_site_access (
    id BIGSERIAL PRIMARY KEY,
    client_user_id BIGINT,
    site_id BIGINT NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT fk_client_site_access_client
        FOREIGN KEY (client_user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_client_site_access_site
        FOREIGN KEY (site_id)
        REFERENCES sites(id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_client_site_access_client
ON client_site_access(client_user_id);

CREATE INDEX IF NOT EXISTS idx_client_site_access_site
ON client_site_access(site_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_client_site_access_active
ON client_site_access(client_user_id, site_id)
WHERE revoked_at IS NULL;

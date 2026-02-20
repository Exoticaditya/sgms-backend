SET search_path TO public;

-- ---------- clients ----------
CREATE TABLE client_accounts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

CREATE TRIGGER trg_client_accounts_updated_at
BEFORE UPDATE ON client_accounts
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- sites ----------
CREATE TABLE sites (
    id BIGSERIAL PRIMARY KEY,
    client_account_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    latitude NUMERIC(10,8),
    longitude NUMERIC(11,8),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_sites_client
        FOREIGN KEY (client_account_id) REFERENCES client_accounts(id) ON DELETE RESTRICT
);

CREATE INDEX idx_sites_client ON sites(client_account_id);

CREATE TRIGGER trg_sites_updated_at
BEFORE UPDATE ON sites
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- site posts ----------
CREATE TABLE site_posts (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL,
    post_name VARCHAR(255) NOT NULL,
    description TEXT,
    required_guards INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_site_posts_site
        FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE RESTRICT,

    CONSTRAINT uq_site_posts UNIQUE(site_id, post_name)
);

CREATE TRIGGER trg_site_posts_updated_at
BEFORE UPDATE ON site_posts
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

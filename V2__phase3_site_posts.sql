-- =====================================================
-- PHASE 3: Site Post Management
-- Migration Script V2
-- =====================================================
-- Description:
--   Creates tables for managing posts within sites,
--   supervisor-to-site assignments, and client site access.
--
-- Tables Created:
--   1. site_posts - Guard duty stations within a site
--   2. supervisor_site_mapping - Supervisor site assignments
--   3. client_site_access - Client access to specific sites
--
-- Prerequisites:
--   - sites table must exist (PHASE 2)
--   - users table must exist (PHASE 1)
--
-- Execution:
--   Run this on Railway PostgreSQL BEFORE deploying code
-- =====================================================

SET search_path TO public;

-- =====================================================
-- 1. SITE_POSTS TABLE
-- =====================================================
-- Represents guard duty posts/stations within a site
-- Example: "Main Gate", "Lobby", "Parking Area"

CREATE TABLE IF NOT EXISTS site_posts (
  id BIGSERIAL PRIMARY KEY,
  site_id BIGINT NOT NULL,
  post_name VARCHAR(255) NOT NULL,
  description TEXT,
  required_guards INT NOT NULL DEFAULT 1,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  
  -- Foreign key to sites table
  CONSTRAINT fk_site_posts_site 
    FOREIGN KEY (site_id) 
    REFERENCES sites(id) 
    ON DELETE RESTRICT,
    
  -- Unique constraint: post names must be unique per site
  CONSTRAINT uq_site_posts_site_post_name 
    UNIQUE (site_id, post_name),
    
  -- Business validation
  CONSTRAINT chk_required_guards_positive 
    CHECK (required_guards > 0)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_site_posts_site_id 
  ON site_posts(site_id);

CREATE INDEX IF NOT EXISTS idx_site_posts_status 
  ON site_posts(status);

CREATE INDEX IF NOT EXISTS idx_site_posts_deleted_at 
  ON site_posts(deleted_at);

COMMENT ON TABLE site_posts IS 'Guard duty stations/posts within a site';
COMMENT ON COLUMN site_posts.post_name IS 'Name of the post (e.g., Main Gate, Lobby)';
COMMENT ON COLUMN site_posts.required_guards IS 'Number of guards required at this post';
COMMENT ON COLUMN site_posts.status IS 'ACTIVE, INACTIVE, CLOSED';

-- =====================================================
-- 2. SUPERVISOR_SITE_MAPPING TABLE
-- =====================================================
-- Maps supervisors to sites they oversee

CREATE TABLE IF NOT EXISTS supervisor_site_mapping (
  id BIGSERIAL PRIMARY KEY,
  supervisor_user_id BIGINT NOT NULL,
  site_id BIGINT NOT NULL,
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  removed_at TIMESTAMPTZ,
  
  -- Foreign key to users table (supervisor must be a user)
  CONSTRAINT fk_supervisor_site_supervisor 
    FOREIGN KEY (supervisor_user_id) 
    REFERENCES users(id) 
    ON DELETE RESTRICT,
    
  -- Foreign key to sites table
  CONSTRAINT fk_supervisor_site_site 
    FOREIGN KEY (site_id) 
    REFERENCES sites(id) 
    ON DELETE RESTRICT,
    
  -- Unique constraint: prevent duplicate active assignments
  -- A supervisor can only be assigned to a site once (active)
  CONSTRAINT uq_supervisor_site_active 
    UNIQUE (supervisor_user_id, site_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_supervisor_site_supervisor_id 
  ON supervisor_site_mapping(supervisor_user_id);

CREATE INDEX IF NOT EXISTS idx_supervisor_site_site_id 
  ON supervisor_site_mapping(site_id);

CREATE INDEX IF NOT EXISTS idx_supervisor_site_removed_at 
  ON supervisor_site_mapping(removed_at);

COMMENT ON TABLE supervisor_site_mapping IS 'Maps supervisors to sites they manage';
COMMENT ON COLUMN supervisor_site_mapping.removed_at IS 'NULL = active, NOT NULL = removed';

-- =====================================================
-- 3. CLIENT_SITE_ACCESS TABLE
-- =====================================================
-- Grants client users access to view specific sites

CREATE TABLE IF NOT EXISTS client_site_access (
  id BIGSERIAL PRIMARY KEY,
  client_user_id BIGINT NOT NULL,
  site_id BIGINT NOT NULL,
  granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  revoked_at TIMESTAMPTZ,
  
  -- Foreign key to users table (client must be a user)
  CONSTRAINT fk_client_site_client 
    FOREIGN KEY (client_user_id) 
    REFERENCES users(id) 
    ON DELETE RESTRICT,
    
  -- Foreign key to sites table
  CONSTRAINT fk_client_site_site 
    FOREIGN KEY (site_id) 
    REFERENCES sites(id) 
    ON DELETE RESTRICT,
    
  -- Unique constraint: prevent duplicate active access grants
  CONSTRAINT uq_client_site_active 
    UNIQUE (client_user_id, site_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_client_site_client_id 
  ON client_site_access(client_user_id);

CREATE INDEX IF NOT EXISTS idx_client_site_site_id 
  ON client_site_access(site_id);

CREATE INDEX IF NOT EXISTS idx_client_site_revoked_at 
  ON client_site_access(revoked_at);

COMMENT ON TABLE client_site_access IS 'Grants client users access to specific sites';
COMMENT ON COLUMN client_site_access.revoked_at IS 'NULL = active, NOT NULL = revoked';

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check all tables created
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('site_posts', 'supervisor_site_mapping', 'client_site_access')
ORDER BY table_name;

-- Verify foreign key constraints
SELECT
  conname AS constraint_name,
  conrelid::regclass AS table_name,
  confrelid::regclass AS referenced_table
FROM pg_constraint
WHERE conname IN (
  'fk_site_posts_site',
  'fk_supervisor_site_supervisor',
  'fk_supervisor_site_site',
  'fk_client_site_client',
  'fk_client_site_site'
)
ORDER BY conname;

-- =====================================================
-- SEED DATA (OPTIONAL - FOR TESTING)
-- =====================================================

-- Example: Create test site posts
-- Assumes you have sites from PHASE 2

-- INSERT INTO site_posts (site_id, post_name, description, required_guards)
-- SELECT 
--   s.id,
--   'Main Entrance',
--   'Primary entry point - 24/7 monitoring required',
--   2
-- FROM sites s
-- WHERE s.name = 'Headquarters' 
--   AND s.deleted_at IS NULL
-- LIMIT 1
-- ON CONFLICT (site_id, post_name) DO NOTHING;

-- Example: Assign supervisor to site
-- Assumes you have a user with SUPERVISOR role

-- INSERT INTO supervisor_site_mapping (supervisor_user_id, site_id)
-- SELECT 
--   u.id AS supervisor_id,
--   s.id AS site_id
-- FROM users u
-- JOIN user_roles ur ON u.id = ur.user_id
-- JOIN roles r ON ur.role_id = r.id
-- CROSS JOIN sites s
-- WHERE r.name = 'SUPERVISOR'
--   AND u.deleted_at IS NULL
--   AND s.deleted_at IS NULL
-- LIMIT 1
-- ON CONFLICT (supervisor_user_id, site_id) DO NOTHING;

-- =====================================================
-- END OF MIGRATION
-- =====================================================

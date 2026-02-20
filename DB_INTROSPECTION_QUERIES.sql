-- =====================================================
-- SGMS DATABASE INTROSPECTION SCRIPT
-- =====================================================
-- Purpose: Diagnose database schema to detect missing tables,
--          orphan records, constraint violations, and mismatches
-- Date: 2026-02-18
-- Execute these queries against Railway PostgreSQL production database
-- =====================================================

-- =====================================================
-- SECTION 1: CURRENT SCHEMA OVERVIEW
-- =====================================================

-- Query 1.1: List all user-created tables
SELECT 
    table_name,
    (SELECT COUNT(*) FROM information_schema.columns c WHERE c.table_name = t.table_name) as column_count
FROM information_schema.tables t
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- Query 1.2: List all columns for each table
SELECT 
    table_name,
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

-- Query 1.3: List all foreign key constraints
SELECT
    tc.constraint_name,
    tc.table_name AS source_table,
    kcu.column_name AS source_column,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_name;

-- Query 1.4: List all unique constraints
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.constraint_type = 'UNIQUE'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_name;

-- Query 1.5: List all indexes
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- Query 1.6: List all ENUM types
SELECT 
    t.typname AS enum_name,
    e.enumlabel AS enum_value
FROM pg_type t
JOIN pg_enum e ON t.oid = e.enumtypid
WHERE t.typtype = 'e'
ORDER BY t.typname, e.enumsortorder;

-- Query 1.7: List all views
SELECT 
    table_name AS view_name,
    view_definition
FROM information_schema.views
WHERE table_schema = 'public'
ORDER BY table_name;

-- =====================================================
-- SECTION 2: TABLE EXISTENCE CHECKS
-- =====================================================

-- Query 2.1: Check for expected core tables
SELECT 
    table_name,
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = expected_table) 
        THEN 'EXISTS'
        ELSE 'MISSING'
    END AS status
FROM (VALUES 
    ('roles'),
    ('users'),
    ('user_roles'),
    ('client_accounts'),
    ('sites'),
    ('site_posts'),
    ('guards'),
    ('shift_types'),
    ('guard_assignments'),
    ('attendance_logs'),
    ('supervisor_site_mapping'),
    ('client_site_access')
) AS expected(expected_table)
ORDER BY expected_table;

-- =====================================================
-- SECTION 3: DATA INTEGRITY CHECKS
-- =====================================================

-- Query 3.1: Check for orphan records in user_roles
SELECT 
    'user_roles -> roles' AS relationship,
    COUNT(*) AS orphan_count
FROM user_roles ur
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.id = ur.role_id)
UNION ALL
SELECT 
    'user_roles -> users' AS relationship,
    COUNT(*) AS orphan_count
FROM user_roles ur
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = ur.user_id);

-- Query 3.2: Check for orphan guards (if tables exist)
-- Run only if guards table exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'guards') THEN
        RAISE NOTICE 'Checking guards orphan records...';
        
        -- Check guards pointing to non-existent users
        PERFORM COUNT(*) FROM guards g
        WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = g.user_id);
        
        -- Check guards pointing to non-existent supervisors
        PERFORM COUNT(*) FROM guards g
        WHERE g.supervisor_user_id IS NOT NULL
          AND NOT EXISTS (SELECT 1 FROM users u WHERE u.id = g.supervisor_user_id);
    END IF;
END $$;

-- Query 3.3: Check for orphan site_posts (if tables exist)
-- Detects site_posts referencing non-existent sites
SELECT 
    'site_posts -> sites' AS relationship,
    COALESCE(COUNT(*), 0) AS orphan_count
FROM information_schema.tables t1
LEFT JOIN site_posts sp ON true
LEFT JOIN sites s ON sp.site_id = s.id
WHERE t1.table_name = 'site_posts'
  AND sp.id IS NOT NULL
  AND s.id IS NULL;

-- Query 3.4: Check for deleted_at violations
-- Find soft-deleted records that are still referenced
SELECT 
    'users with deleted_at but active guards' AS issue,
    COUNT(*) AS violation_count
FROM users u
INNER JOIN guards g ON u.id = g.user_id
WHERE u.deleted_at IS NOT NULL
  AND g.deleted_at IS NULL;

-- =====================================================
-- SECTION 4: CONSTRAINT VIOLATION DETECTION
-- =====================================================

-- Query 4.1: Find NULL violations in NOT NULL columns
-- Check users table for NULL in required fields
SELECT 
    'users.email' AS column_name,
    COUNT(*) AS null_count
FROM users
WHERE email IS NULL
UNION ALL
SELECT 
    'users.password_hash' AS column_name,
    COUNT(*) AS null_count
FROM users
WHERE password_hash IS NULL
UNION ALL
SELECT 
    'users.status' AS column_name,
    COUNT(*) AS null_count
FROM users
WHERE status IS NULL;

-- Query 4.2: Check for duplicate values in unique columns
-- Check for duplicate emails in users
SELECT 
    email,
    COUNT(*) AS duplicate_count
FROM users
WHERE deleted_at IS NULL
GROUP BY email
HAVING COUNT(*) > 1;

-- Query 4.3: Check for guards without employee_number
-- If employee_number column exists and should be unique
SELECT 
    COUNT(*) AS guards_without_employee_number
FROM guards
WHERE employee_code IS NULL OR employee_code = '';

-- =====================================================
-- SECTION 5: RECORD COUNTS
-- =====================================================

-- Query 5.1: Get record counts for all tables
SELECT 
    'roles' AS table_name,
    (SELECT COUNT(*) FROM roles) AS total_count
UNION ALL
SELECT 
    'users' AS table_name,
    (SELECT COUNT(*) FROM users) AS total_count
UNION ALL
SELECT 
    'user_roles' AS table_name,
    (SELECT COUNT(*) FROM user_roles) AS total_count
UNION ALL
SELECT 
    'guards' AS table_name,
    (SELECT COUNT(*) FROM guards WHERE deleted_at IS NULL) AS total_count
UNION ALL
SELECT 
    'shift_types' AS table_name,
    (SELECT COUNT(*) FROM shift_types) AS total_count
ORDER BY table_name;

-- Query 5.2: Check soft-delete counts
SELECT 
    'users' AS table_name,
    COUNT(*) FILTER (WHERE deleted_at IS NULL) AS active_count,
    COUNT(*) FILTER (WHERE deleted_at IS NOT NULL) AS deleted_count
FROM users
UNION ALL
SELECT 
    'guards' AS table_name,
    COUNT(*) FILTER (WHERE deleted_at IS NULL) AS active_count,
    COUNT(*) FILTER (WHERE deleted_at IS NOT NULL) AS deleted_count
FROM guards;

-- =====================================================
-- SECTION 6: MISSING RELATIONS DETECTION
-- =====================================================

-- Query 6.1: Check if sites table exists and has client_account_id
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'sites'
  AND column_name = 'client_account_id';

-- Query 6.2: Check if client_accounts table exists
SELECT 
    table_name,
    (SELECT COUNT(*) FROM information_schema.columns c WHERE c.table_name = 'client_accounts') as columns
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name = 'client_accounts';

-- =====================================================
-- SECTION 7: FLYWAY MIGRATION STATUS
-- =====================================================

-- Query 7.1: Check Flyway schema history
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;

-- Query 7.2: Detect failed migrations
SELECT 
    version,
    description,
    script,
    installed_on
FROM flyway_schema_history
WHERE success = false
ORDER BY installed_on DESC;

-- =====================================================
-- SECTION 8: CRITICAL MISSING TABLES REPORT
-- =====================================================

-- Query 8.1: Generate missing tables report
WITH expected_tables AS (
    SELECT unnest(ARRAY[
        'roles', 'users', 'user_roles',
        'client_accounts', 'sites', 'site_posts',
        'guards', 'shift_types', 'guard_assignments',
        'attendance_logs', 'supervisor_site_mapping',
        'client_site_access'
    ]) AS table_name
),
existing_tables AS (
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'public'
)
SELECT 
    et.table_name,
    CASE 
        WHEN ext.table_name IS NOT NULL THEN '✓ EXISTS'
        ELSE '✗ MISSING'
    END AS status,
    CASE 
        WHEN et.table_name IN ('client_accounts', 'sites') THEN 'CRITICAL - Required by entities'
        WHEN et.table_name IN ('attendance_logs') THEN 'IMPORTANT - V4 migration file exists'
        ELSE 'NORMAL'
    END AS severity
FROM expected_tables et
LEFT JOIN existing_tables ext ON et.table_name = ext.table_name
ORDER BY 
    CASE 
        WHEN ext.table_name IS NULL THEN 0
        ELSE 1
    END,
    et.table_name;

-- =====================================================
-- END OF INTROSPECTION QUERIES
-- =====================================================

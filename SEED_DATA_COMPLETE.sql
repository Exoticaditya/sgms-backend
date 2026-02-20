-- =====================================================
-- SGMS COMPREHENSIVE DATA SEEDING SCRIPT
-- =====================================================
-- Purpose: Populate database with realistic test data
-- Date: 2026-02-18
-- Execution: Run AFTER all migrations (V0-V4) complete successfully
-- 
-- Data Insertion Order (respects foreign key dependencies):
-- 1. roles (already seeded in V1)
-- 2. users
-- 3. user_roles  
-- 4. client_accounts (already seeded in V0)
-- 5. sites (sample already in V0)
-- 6. site_posts
-- 7. guards
-- 8. shift_types (already seeded in V3)
-- 9. guard_assignments
-- 10. attendance_logs (optional - for testing)
-- =====================================================

SET search_path TO public;

-- =====================================================
-- SECTION 1: USER ACCOUNTS
-- =====================================================
-- Create users for different roles

-- Admin user (already created in V1)
-- Email: admin@zplusesecurity.com
-- Password: admin123

-- Supervisor user
INSERT INTO users (email, phone, password_hash, full_name, status, created_at, updated_at)
VALUES (
  'supervisor@zplusesecurity.com',
  '+1-555-1001',
  crypt('supervisor123', gen_salt('bf')),
  'Michael Johnson',
  'ACTIVE',
  NOW(),
  NOW()
)
ON CONFLICT DO NOTHING;

-- Manager user
INSERT INTO users (email, phone, password_hash, full_name, status, created_at, updated_at)
VALUES (
  'manager@zplusesecurity.com',
  '+1-555-1002',
  crypt('manager123', gen_salt('bf')),
  'Sarah Williams',
  'ACTIVE',
  NOW(),
  NOW()
)
ON CONFLICT DO NOTHING;

-- Client user (Demo Corp)
INSERT INTO users (email, phone, password_hash, full_name, status, created_at, updated_at)
VALUES (
  'client@democorp.com',
  '+1-555-2001',
  crypt('client123', gen_salt('bf')),
  'John Doe',
  'ACTIVE',
  NOW(),
  NOW()
)
ON CONFLICT DO NOTHING;

-- Guard users (5 sample guards)
INSERT INTO users (email, phone, password_hash, full_name, status, created_at, updated_at)
VALUES 
  ('james.smith@zplusesecurity.com', '+1-555-3001', crypt('guard123', gen_salt('bf')), 'James Smith', 'ACTIVE', NOW(), NOW()),
  ('maria.garcia@zplusesecurity.com', '+1-555-3002', crypt('guard123', gen_salt('bf')), 'Maria Garcia', 'ACTIVE', NOW(), NOW()),
  ('robert.brown@zplusesecurity.com', '+1-555-3003', crypt('guard123', gen_salt('bf')), 'Robert Brown', 'ACTIVE', NOW(), NOW()),
  ('emily.davis@zplusesecurity.com', '+1-555-3004', crypt('guard123', gen_salt('bf')), 'Emily Davis', 'ACTIVE', NOW(), NOW()),
  ('david.wilson@zplusesecurity.com', '+1-555-3005', crypt('guard123', gen_salt('bf')), 'David Wilson', 'ACTIVE', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- SECTION 2: USER ROLES
-- =====================================================
-- Assign roles to users

-- Supervisor role
INSERT INTO user_roles (role_id, user_id)
SELECT r.id, u.id
FROM roles r
JOIN users u ON u.email = 'supervisor@zplusesecurity.com'
WHERE r.name = 'SUPERVISOR'
ON CONFLICT DO NOTHING;

-- Manager role (also supervisor)
INSERT INTO user_roles (role_id, user_id)
SELECT r.id, u.id
FROM roles r
JOIN users u ON u.email = 'manager@zplusesecurity.com'
WHERE r.name = 'SUPERVISOR'
ON CONFLICT DO NOTHING;

-- Client role
INSERT INTO user_roles (role_id, user_id)
SELECT r.id, u.id
FROM roles r
JOIN users u ON u.email = 'client@democorp.com'
WHERE r.name = 'CLIENT'
ON CONFLICT DO NOTHING;

-- Guard roles
INSERT INTO user_roles (role_id, user_id)
SELECT r.id, u.id
FROM roles r
CROSS JOIN users u
WHERE r.name = 'GUARD'
  AND u.email LIKE '%@zplusesecurity.com'
  AND u.email NOT IN ('admin@zplusesecurity.com', 'supervisor@zplusesecurity.com', 'manager@zplusesecurity.com')
ON CONFLICT DO NOTHING;

-- =====================================================
-- SECTION 3: CLIENT ACCOUNTS
-- =====================================================
-- Additional client accounts beyond Demo Corp

INSERT INTO client_accounts (
  name, 
  contact_person, 
  contact_email, 
  contact_phone,
  address,
  status,
  contract_start_date,
  created_at,
  updated_at
)
VALUES 
  (
    'TechCorp Industries',
    'Alice Thompson',
    'alice@techcorp.com',
    '+1-555-4001',
    '456 Tech Boulevard, San Francisco, CA 94105',
    'ACTIVE',
    '2025-01-01',
    NOW(),
    NOW()
  ),
  (
    'Retail Solutions LLC',
    'Bob Martinez',
    'bob@retailsolutions.com',
    '+1-555-4002',
    '789 Commerce Street, Chicago, IL 60601',
    'ACTIVE',
    '2025-06-01',
    NOW(),
    NOW()
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- SECTION 4: SITES
-- =====================================================
-- Additional sites for clients

INSERT INTO sites (
  client_account_id,
  name,
  address,
  city,
  state,
  zip_code,
  latitude,
  longitude,
  geofence_radius_meters,
  status,
  site_manager_name,
  site_manager_phone,
  created_at,
  updated_at
)
SELECT 
  ca.id,
  'Demo Corporate Campus East',
  '125 East Business Drive',
  'New York',
  'NY',
  '10002',
  40.7282,
  -73.9942,
  200,
  'ACTIVE',
  'Tom Anderson',
  '+1-555-0201',
  NOW(),
  NOW()
FROM client_accounts ca
WHERE ca.name = 'Demo Corporation'
UNION ALL
SELECT 
  ca.id,
  'TechCorp Data Center',
  '100 Server Farm Road',
  'San Francisco',
  'CA',
  '94105',
  37.7749,
  -122.4194,
  150,
  'ACTIVE',
  'Lisa Chen',
  '+1-555-4101',
  NOW(),
  NOW()
FROM client_accounts ca
WHERE ca.name = 'TechCorp Industries'
UNION ALL
SELECT 
  ca.id,
  'Retail Solutions Warehouse',
  '500 Logistics Avenue',
  'Chicago',
  'IL',
  '60601',
  41.8781,
  -87.6298,
  250,
  'ACTIVE',
  'Kevin Park',
  '+1-555-4201',
  NOW(),
  NOW()
FROM client_accounts ca
WHERE ca.name = 'Retail Solutions LLC'
ON CONFLICT DO NOTHING;

-- =====================================================
-- SECTION 5: SITE POSTS
-- =====================================================
-- Guard duty posts within sites

INSERT INTO site_posts (
  site_id,
  post_name,
  description,
  required_guards,
  status,
  created_at,
  updated_at
)
SELECT 
  s.id,
  unnest(ARRAY['Main Entrance', 'Parking Deck', 'Executive Floor', 'Loading Dock']),
  unnest(ARRAY[
    'Primary entry point - 24/7 coverage required',
    'Vehicle entry/exit monitoring',
    'Executive suite access control',
    'Delivery and shipping area supervision'
  ]),
  unnest(ARRAY[2, 1, 1, 1]),
  'ACTIVE',
  NOW(),
  NOW()
FROM sites s
WHERE s.name = 'Demo Corporate Headquarters'
  AND NOT EXISTS (SELECT 1 FROM site_posts WHERE site_id = s.id)
ON CONFLICT (site_id, post_name) DO NOTHING;

-- Insert posts for other sites
INSERT INTO site_posts (
  site_id,
  post_name,
  description,
  required_guards,
  status,
  created_at,
  updated_at
)
SELECT 
  s.id,
  unnest(ARRAY['Main Gate', 'Lobby Reception']),
  unnest(ARRAY[
    'Primary campus access control',
    'Visitor management and screening'
  ]),
  unnest(ARRAY[1, 1]),
  'ACTIVE',
  NOW(),
  NOW()
FROM sites s
WHERE s.name = 'Demo Corporate Campus East'
  AND NOT EXISTS (SELECT 1 FROM site_posts WHERE site_id = s.id)
ON CONFLICT (site_id, post_name) DO NOTHING;

INSERT INTO site_posts (
  site_id,
  post_name,
  description,
  required_guards,
  status,
  created_at,
  updated_at
)
SELECT 
  s.id,
  unnest(ARRAY['Server Room Access', 'Night Patrol']),
  unnest(ARRAY[
    'Data center entry monitoring',
    'Perimeter patrol and facility check'
  ]),
  unnest(ARRAY[1, 1]),
  'ACTIVE',
  NOW(),
  NOW()
FROM sites s
WHERE s.name = 'TechCorp Data Center'
  AND NOT EXISTS (SELECT 1 FROM site_posts WHERE site_id = s.id)
ON CONFLICT (site_id, post_name) DO NOTHING;

-- =====================================================
-- SECTION 6: GUARDS
-- =====================================================
-- Create guard records linked to user accounts

-- Get supervisor user ID for assignment
DO $$
DECLARE
  v_supervisor_id BIGINT;
BEGIN
  SELECT id INTO v_supervisor_id FROM users WHERE email = 'supervisor@zplusesecurity.com';

  -- Insert guards
  INSERT INTO guards (
    user_id,
    supervisor_user_id,
    employee_code,
    first_name,
    last_name,
    phone,
    status,
    hire_date,
    base_salary,
    per_day_rate,
    overtime_rate,
    created_at,
    updated_at
  )
  SELECT 
    u.id,
    v_supervisor_id,
    'GRD-' || LPAD(ROW_NUMBER() OVER ()::TEXT, 4, '0'),
    SPLIT_PART(u.full_name, ' ', 1),
    SPLIT_PART(u.full_name, ' ', 2),
    u.phone,
    'ACTIVE',
    CURRENT_DATE - INTERVAL '6 months',
    3500.00,
    120.00,
    25.00,
    NOW(),
    NOW()
  FROM users u
  WHERE u.email LIKE '%@zplusesecurity.com'
    AND u.email NOT IN ('admin@zplusesecurity.com', 'supervisor@zplusesecurity.com', 'manager@zplusesecurity.com')
    AND NOT EXISTS (SELECT 1 FROM guards WHERE user_id = u.id)
  ON CONFLICT (user_id) DO NOTHING;
END $$;

-- =====================================================
-- SECTION 7: SUPERVISOR SITE MAPPINGS
-- =====================================================
-- Assign supervisors to sites they manage

INSERT INTO supervisor_site_mapping (
  supervisor_user_id,
  site_id,
  assigned_at
)
SELECT 
  u.id,
  s.id,
  NOW()
FROM users u
CROSS JOIN sites s
WHERE u.email = 'supervisor@zplusesecurity.com'
  AND s.status = 'ACTIVE'
  AND s.deleted_at IS NULL
ON CONFLICT (supervisor_user_id, site_id) DO NOTHING;

-- =====================================================
-- SECTION 8: CLIENT SITE ACCESS
-- =====================================================
-- Grant client users access to their sites

INSERT INTO client_site_access (
  client_user_id,
  site_id,
  granted_at
)
SELECT 
  u.id,
  s.id,
  NOW()
FROM users u
JOIN client_accounts ca ON LOWER(u.email) LIKE '%' || LOWER(SPLIT_PART(ca.contact_email, '@', 2))
JOIN sites s ON s.client_account_id = ca.id
WHERE u.email = 'client@democorp.com'
  AND s.deleted_at IS NULL
ON CONFLICT (client_user_id, site_id) DO NOTHING;

-- =====================================================
-- SECTION 9: GUARD ASSIGNMENTS
-- =====================================================
-- Deploy guards to site posts

DO $$
DECLARE
  v_admin_id BIGINT;
  v_day_shift_id BIGINT;
  v_night_shift_id BIGINT;
  v_evening_shift_id BIGINT;
BEGIN
  -- Get admin user for created_by
  SELECT id INTO v_admin_id FROM users WHERE email = 'admin@zplusesecurity.com';
  
  -- Get shift type IDs
  SELECT id INTO v_day_shift_id FROM shift_types WHERE name = 'DAY';
  SELECT id INTO v_night_shift_id FROM shift_types WHERE name = 'NIGHT';
  SELECT id INTO v_evening_shift_id FROM shift_types WHERE name = 'EVENING';

  -- Assign guards to posts
  -- Post: Main Entrance (needs 2 guards - day and night shift)
  INSERT INTO guard_assignments (
    guard_id,
    site_post_id,
    shift_type_id,
    effective_from,
    effective_to,
    status,
    notes,
    created_at,
    updated_at,
    created_by_user_id
  )
  SELECT 
    g.id,
    sp.id,
    v_day_shift_id,
    CURRENT_DATE,
    NULL,
    'ACTIVE',
    'Primary entrance coverage - Day shift',
    NOW(),
    NOW(),
    v_admin_id
  FROM guards g
  JOIN site_posts sp ON sp.post_name = 'Main Entrance'
  WHERE g.employee_code = 'GRD-0001'
    AND NOT EXISTS (
      SELECT 1 FROM guard_assignments WHERE guard_id = g.id AND site_post_id = sp.id
    )
  LIMIT 1;

  INSERT INTO guard_assignments (
    guard_id,
    site_post_id,
    shift_type_id,
    effective_from,
    effective_to,
    status,
    notes,
    created_at,
    updated_at,
    created_by_user_id
  )
  SELECT 
    g.id,
    sp.id,
    v_night_shift_id,
    CURRENT_DATE,
    NULL,
    'ACTIVE',
    'Primary entrance coverage - Night shift',
    NOW(),
    NOW(),
    v_admin_id
  FROM guards g
  JOIN site_posts sp ON sp.post_name = 'Main Entrance'
  WHERE g.employee_code = 'GRD-0002'
    AND NOT EXISTS (
      SELECT 1 FROM guard_assignments WHERE guard_id = g.id AND site_post_id = sp.id
    )
  LIMIT 1;

  -- Other posts - one guard each
  INSERT INTO guard_assignments (
    guard_id,
    site_post_id,
    shift_type_id,
    effective_from,
    effective_to,
    status,
    notes,
    created_at,
    updated_at,
    created_by_user_id
  )
  SELECT 
    g.id,
    sp.id,
    CASE 
      WHEN sp.post_name = 'Parking Deck' THEN v_day_shift_id
      WHEN sp.post_name = 'Executive Floor' THEN v_day_shift_id
      WHEN sp.post_name = 'Loading Dock' THEN v_evening_shift_id
    END,
    CURRENT_DATE,
    NULL,
    'ACTIVE',
    'Standard post assignment',
    NOW(),
    NOW(),
    v_admin_id
  FROM guards g
  CROSS JOIN site_posts sp
  WHERE sp.post_name IN ('Parking Deck', 'Executive Floor', 'Loading Dock')
    AND g.employee_code IN ('GRD-0003', 'GRD-0004', 'GRD-0005')
    AND NOT EXISTS (
      SELECT 1 FROM guard_assignments WHERE guard_id = g.id
    )
  LIMIT 3;

END $$;

-- =====================================================
-- SECTION 10: ATTENDANCE LOGS (SAMPLE DATA)
-- =====================================================
-- Create sample attendance records for testing

DO $$
DECLARE
  v_assignment_id BIGINT;
  v_guard_id BIGINT;
  v_shift_start TIME;
  v_shift_end TIME;
BEGIN
  -- Create attendance for each active assignment
  FOR v_assignment_id, v_guard_id, v_shift_start, v_shift_end IN
    SELECT 
      ga.id,
      ga.guard_id,
      st.start_time,
      st.end_time
    FROM guard_assignments ga
    JOIN shift_types st ON ga.shift_type_id = st.id
    WHERE ga.status = 'ACTIVE'
      AND ga.effective_from <= CURRENT_DATE
      AND (ga.effective_to IS NULL OR ga.effective_to >= CURRENT_DATE)
  LOOP
    -- Yesterday's attendance (all checked in and out)
    INSERT INTO attendance_logs (
      guard_id,
      assignment_id,
      attendance_date,
      check_in_time,
      check_out_time,
      status,
      late_minutes,
      early_leave_minutes,
      created_at,
      updated_at
    )
    VALUES (
      v_guard_id,
      v_assignment_id,
      CURRENT_DATE - 1,
      (CURRENT_DATE - 1)::TIMESTAMP + v_shift_start,
      (CURRENT_DATE - 1)::TIMESTAMP + v_shift_end,
      'PRESENT',
      0,
      0,
      NOW(),
      NOW()
    )
    ON CONFLICT (guard_id, attendance_date) DO NOTHING;

    -- Today's attendance (checked in, not yet checked out)
    INSERT INTO attendance_logs (
      guard_id,
      assignment_id,
      attendance_date,
      check_in_time,
      check_out_time,
      status,
      late_minutes,
      early_leave_minutes,
      created_at,
      updated_at
    )
    VALUES (
      v_guard_id,
      v_assignment_id,
      CURRENT_DATE,
      CURRENT_DATE::TIMESTAMP + v_shift_start,
      NULL,
      'PRESENT',
      0,
      0,
      NOW(),
      NOW()
    )
    ON CONFLICT (guard_id, attendance_date) DO NOTHING;
  END LOOP;
END $$;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check record counts
SELECT 
  'users' AS table_name,
  COUNT(*) AS total,
  COUNT(*) FILTER (WHERE deleted_at IS NULL) AS active
FROM users
UNION ALL
SELECT 
  'client_accounts' AS table_name,
  COUNT(*) AS total,
  COUNT(*) FILTER (WHERE deleted_at IS NULL) AS active
FROM client_accounts
UNION ALL
SELECT 
  'sites' AS table_name,
  COUNT(*) AS total,
  COUNT(*) FILTER (WHERE deleted_at IS NULL) AS active
FROM sites
UNION ALL
SELECT 
  'site_posts' AS table_name,
  COUNT(*) AS total,
  COUNT(*) FILTER (WHERE deleted_at IS NULL) AS active
FROM site_posts
UNION ALL
SELECT 
  'guards' AS table_name,
  COUNT(*) AS total,
  COUNT(*) FILTER (WHERE deleted_at IS NULL) AS active
FROM guards
UNION ALL
SELECT 
  'guard_assignments' AS table_name,
  COUNT(*) AS total,
  COUNT(*) FILTER (WHERE status = 'ACTIVE') AS active
FROM guard_assignments
UNION ALL
SELECT 
  'attendance_logs' AS table_name,
  COUNT(*) AS total,
  COUNT(*) FILTER (WHERE attendance_date = CURRENT_DATE) AS today
FROM attendance_logs;

-- Show users by role
SELECT 
  r.name AS role,
  COUNT(ur.user_id) AS user_count
FROM roles r
LEFT JOIN user_roles ur ON r.id = ur.role_id
GROUP BY r.name
ORDER BY r.name;

-- Show guard assignments summary
SELECT 
  s.name AS site_name,
  sp.post_name,
  st.name AS shift,
  g.first_name || ' ' || g.last_name AS guard_name,
  ga.status
FROM guard_assignments ga
JOIN guards g ON ga.guard_id = g.id
JOIN site_posts sp ON ga.site_post_id = sp.id
JOIN sites s ON sp.site_id = s.id
JOIN shift_types st ON ga.shift_type_id = st.id
WHERE ga.status = 'ACTIVE'
ORDER BY s.name, sp.post_name, st.start_time;

-- =====================================================
-- END OF SEEDING SCRIPT
-- =====================================================

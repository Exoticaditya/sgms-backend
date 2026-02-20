# SGMS SCHEMA ANALYSIS REPORT
**Generated:** 2026-02-18  
**System:** Security Guard Management System (SGMS)  
**Database:** PostgreSQL on Railway  
**Architecture:** Database-first (Hibernate ddl-auto: none)

---

## EXECUTIVE SUMMARY

### Critical Issues Found
1. **MISSING TABLES:** `client_accounts` and `sites` tables do not exist in migrations
2. **MISPLACED MIGRATIONS:** V1, V2, V3 are in wrong directory (backend root instead of `src/main/resources/db/migration`)
3. **BROKEN FOREIGN KEYS:** Multiple tables reference non-existent `client_accounts` and `sites`
4. **APPLICATION STARTUP WILL FAIL:** Entities expect tables that don't exist

---

## 1. DATABASE SCHEMA OVERVIEW

### 1.1 Expected Tables (from Entity Classes)
```
Core Authentication:
✓ roles                      - User roles (ADMIN, SUPERVISOR, GUARD, CLIENT)
✓ users                      - User accounts with auth credentials
✓ user_roles                 - Many-to-many mapping

Client Management:
✗ client_accounts            - MISSING - Client companies
✗ sites                      - MISSING - Client sites/locations
✓ site_posts                 - Guard duty posts within sites
✓ supervisor_site_mapping    - Supervisor-to-site assignments
✓ client_site_access         - Client user site permissions

Guard Management:
✓ guards                     - Guard employees
✓ shift_types                - Shift definitions (DAY, NIGHT, EVENING)
✓ guard_assignments          - Guard-to-post deployments
✗ attendance_logs            - MIGRATION EXISTS but not in correct folder
```

### 1.2 Migration Files Status

| Migration | Location | Status | Tables Created |
|-----------|----------|--------|----------------|
| V1__initial_schema.sql | ❌ Backend root | **MISPLACED** | roles, users, user_roles, guards |
| V2__phase3_site_posts.sql | ❌ Backend root | **MISPLACED** | site_posts, supervisor_site_mapping, client_site_access |
| V3__phase_a_guard_deployment.sql | ❌ Backend root | **MISPLACED** | shift_types, guard_assignments |
| V4__phase_b_attendance_tracking.sql | ✅ Correct location | **OK** | attendance_logs |
| **Missing: V0_client_and_sites.sql** | - | **DOES NOT EXIST** | client_accounts, sites |

---

## 2. CRITICAL MISSING TABLES

### 2.1 client_accounts Table

**Referenced by:**
- `SiteEntity.java` - ManyToOne relationship
- `V3__phase_a_guard_deployment.sql` - View joins this table
- Backend expects this for client management

**Expected Schema (from SiteEntity):**
```sql
CREATE TABLE client_accounts (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  deleted_at TIMESTAMPTZ
);
```

**Why it's missing:**
- No migration file creates this table
- Likely created manually or assumed to exist
- Application will fail on startup when JPA tries to validate schema

---

### 2.2 sites Table

**Referenced by:**
- `SitePostEntity.java` - ManyToOne relationship  
- `SupervisorSiteMappingEntity.java` - Foreign key
- `ClientSiteAccessEntity.java` - Foreign key
- `V3__phase_a_guard_deployment.sql` - Views join this table

**Expected Schema (from SiteEntity):**
```sql
CREATE TABLE sites (
  id BIGSERIAL PRIMARY KEY,
  client_account_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  address VARCHAR(500),
  latitude NUMERIC(10, 8),
  longitude NUMERIC(11, 8),
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  deleted_at TIMESTAMPTZ,
  
  CONSTRAINT fk_sites_client_account 
    FOREIGN KEY (client_account_id) 
    REFERENCES client_accounts(id)
);
```

**Why it's missing:**
- No migration file creates this table
- V2 expects it to exist (creates site_posts with FK to sites)
- CASCADE FAILURE: site_posts, supervisor_site_mapping, client_site_access all broken

---

## 3. DEPENDENCY CHAIN ANALYSIS

### 3.1 Correct Creation Order

```
1. roles
2. users
3. user_roles
4. client_accounts          ← MISSING
5. sites                    ← MISSING (depends on client_accounts)
6. site_posts               (depends on sites)
7. guards                   (depends on users)
8. shift_types
9. guard_assignments        (depends on guards, site_posts, shift_types, users)
10. attendance_logs         (depends on guards, guard_assignments)
11. supervisor_site_mapping (depends on users, sites)
12. client_site_access      (depends on users, sites)
```

### 3.2 Current Migration Execution Order (BROKEN)

```
V1: roles, users, user_roles, guards ✓
V2: site_posts ✗ (sites doesn't exist - FK constraint will fail)
    supervisor_site_mapping ✗ (sites doesn't exist)
    client_site_access ✗ (sites doesn't exist)
V3: shift_types ✓
    guard_assignments ✗ (site_posts broken, creates broken view)
V4: attendance_logs ✓ (but depends on guard_assignments which is broken)
```

**RESULT:** Migrations V2-V4 will fail due to missing prerequisites

---

## 4. FOREIGN KEY VIOLATIONS

### 4.1 Broken Relationships

| Source Table | Column | References | Status |
|--------------|--------|------------|--------|
| sites | client_account_id | client_accounts(id) | ❌ Target table missing |
| site_posts | site_id | sites(id) | ❌ Target table missing |
| supervisor_site_mapping | site_id | sites(id) | ❌ Target table missing |
| client_site_access | site_id | sites(id) | ❌ Target table missing |
| guard_assignments | site_post_id | site_posts(id) | ⚠️ Depends on broken sites FK |
| attendance_logs | assignment_id | guard_assignments(id) | ⚠️ Transitively broken |

### 4.2 Impact on Application

```java
// This will FAIL at runtime
@ManyToOne
@JoinColumn(name = "client_account_id", nullable = false)
private ClientAccountEntity clientAccount;  // Table doesn't exist!

// This too
@ManyToOne
@JoinColumn(name = "site_id", nullable = false) 
private SiteEntity site;  // Table doesn't exist!
```

---

## 5. ENTITY vs DATABASE MISMATCH

### 5.1 GuardEntity Discrepancy

**Database (V1_initial_schema.sql):**
```sql
employee_code VARCHAR(50) NOT NULL UNIQUE
```

**Entity (GuardEntity.java):**
```java
@Column(name = "employee_code", nullable = false, unique = true)
private String employeeCode;
```
✅ **MATCH** - No issue

---

### 5.2 AttendanceEntity Type Mismatch

**Database (V4_phase_b_attendance_tracking.sql):**
```sql
attendance_date DATE NOT NULL
check_in_time TIMESTAMP
check_out_time TIMESTAMP
```

**Entity (AttendanceEntity.java):**
```java
@Column(name = "attendance_date", nullable = false)
private LocalDate attendanceDate;  // ✓ Maps to DATE

@Column(name = "check_in_time")
private Instant checkInTime;  // ✓ Maps to TIMESTAMP

@Column(name = "check_out_time")
private Instant checkOutTime;  // ✓ Maps to TIMESTAMP
```
✅ **MATCH** - Correct mapping

---

### 5.3 Enum Type Handling

**Database:**
```sql
CREATE TYPE attendance_status AS ENUM (
    'PRESENT', 'LATE', 'EARLY_LEAVE', 'ABSENT', 'MISSED_CHECKOUT'
);
```

**Entity:**
```java
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
private AttendanceStatus status;
```

**Java Enum:**
```java
public enum AttendanceStatus {
  PRESENT, LATE, EARLY_LEAVE, ABSENT, MISSED_CHECKOUT
}
```
✅ **MATCH** - EnumType.STRING will work with PostgreSQL ENUM

---

## 6. SOFT DELETE IMPLEMENTATION

### 6.1 Tables with deleted_at

```
✓ users.deleted_at
✓ guards.deleted_at
✗ client_accounts.deleted_at (table missing)
✗ sites.deleted_at (table missing)
✓ site_posts.deleted_at
```

### 6.2 Repository Soft Delete Compliance

**CHECK REQUIRED:** All repositories must filter `WHERE deleted_at IS NULL`

Example from expected pattern:
```java
@Query("SELECT g FROM GuardEntity g WHERE g.deletedAt IS NULL")
List<GuardEntity> findAllActive();
```

---

## 7. INDEX AND PERFORMANCE ANALYSIS

### 7.1 Indexes Created by Migrations

| Table | Index | Purpose |
|-------|-------|---------|
| user_roles | idx_user_roles_role_id | FK lookup |
| user_roles | idx_user_roles_user_id | FK lookup |
| guards | idx_guards_supervisor_user_id | FK lookup |
| site_posts | idx_site_posts_site_id | FK lookup |
| site_posts | idx_site_posts_status | Filtering |
| site_posts | idx_site_posts_deleted_at | Soft delete queries |
| guard_assignments | Multiple composite indexes | Complex queries |
| attendance_logs | idx_attendance_guard_id | Guard lookups |
| attendance_logs | idx_attendance_date | Date range queries |
| attendance_logs | idx_attendance_pending_checkout | Scheduler optimization |

✅ **GOOD:** Comprehensive indexing strategy in place

---

## 8. VIEWS AND DENORMALIZATION

### 8.1 Views Created

| View | Purpose | Status |
|------|---------|--------|
| v_active_guard_assignments | Denormalized assignment details | ❌ Joins non-existent tables |
| v_today_attendance_summary | Today's attendance dashboard | ✅ Should work if dependencies fixed |
| v_pending_checkins_today | Guards who haven't checked in | ✅ OK |
| v_pending_checkouts_today | Guards who haven't checked out | ✅ OK |

**Issue:** `v_active_guard_assignments` joins `client_accounts` and `sites` which don't exist:
```sql
JOIN sites s ON sp.site_id = s.id
JOIN client_accounts ca ON s.client_account_id = ca.id
```

---

## 9. CONFIGURATION VERIFICATION

### 9.1 Hibernate Configuration
```yaml
# application-local.yml
jpa:
  hibernate:
    ddl-auto: none  ✅ CORRECT - Database-first approach

# application-prod.yml  
jpa:
  hibernate:
    ddl-auto: none  ✅ CORRECT
```

### 9.2 Flyway Configuration
- **Location:** Expected at `src/main/resources/db/migration/`
- **Current Issue:** V1, V2, V3 in wrong directory
- **Risk:** Flyway won't find them, migrations won't run

---

## 10. SEED DATA ANALYSIS

### 10.1 Seeding in Migrations

**V1 includes:**
```sql
INSERT INTO roles (name) VALUES ('ADMIN'), ('SUPERVISOR'), ('GUARD'), ('CLIENT');
INSERT INTO users (...) VALUES ('admin@zplusesecurity.com', ...);
INSERT INTO user_roles (...) -- Admin role assignment
```
✅ **GOOD:** Bootstrap data included

**V3 includes:**
```sql
INSERT INTO shift_types (name, start_time, end_time, description)
VALUES 
  ('DAY', '06:00:00', '14:00:00', ...),
  ('EVENING', '14:00:00', '22:00:00', ...),
  ('NIGHT', '22:00:00', '06:00:00', ...);
```
✅ **GOOD:** Reference data included

---

## RECOMMENDATIONS

### Immediate Actions Required

1. **CREATE MISSING MIGRATION V0**
   - Create `V0__client_and_sites.sql`
   - Define `client_accounts` and `sites` tables
   - Place in `src/main/resources/db/migration/`

2. **RELOCATE EXISTING MIGRATIONS**
   - Move V1, V2, V3 from backend root to `src/main/resources/db/migration/`
   - Ensure correct execution order

3. **FIX VIEW DEFINITIONS**
   - Update `v_active_guard_assignments` to reference correct tables
   - Add error handling for missing data

4. **VERIFY REPOSITORIES**
   - Audit all Repository classes for soft delete filtering
   - Ensure all queries include `WHERE deleted_at IS NULL`

5. **ADD MISSING INDEXES**
   - Index on `sites.client_account_id`
   - Index on `sites.deleted_at`
   - Index on `client_accounts.deleted_at`

---

## NEXT STEPS: PHASE 2 - BACKEND AUDIT

After fixing database schema:
1. Verify all Entity classes match database exactly
2. Check Repository method queries
3. Audit Service layer for soft delete compliance
4. Test JWT authentication flow
5. Verify role-based access control
6. Check scheduler jobs (attendance auto-marking)

---

**END OF REPORT**

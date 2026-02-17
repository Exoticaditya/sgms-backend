# PHASE 2 - Site Management Deployment Guide

**Module**: Site Management (Clients & Sites)  
**Status**: Ready for deployment  
**Date**: February 2026

---

## 🚀 PRE-DEPLOYMENT CHECKLIST

### ✅ Code Status
- [✅] All files created (12 new files)
- [✅] Compilation successful (BUILD SUCCESS)
- [✅] No compilation errors
- [✅] StartupValidation updated with new tables
- [✅] Documentation complete

### ⚠️ Database Migration Required

**CRITICAL**: Run these SQL scripts on Railway PostgreSQL **BEFORE** deploying the new code.

---

## 📊 DATABASE MIGRATION

### Connect to Railway PostgreSQL

```bash
# Get database credentials from Railway dashboard
# Method 1: Railway CLI
railway connect postgres

# Method 2: psql
psql postgresql://postgres:password@host.railway.app:5432/railway
```

---

### Step 1: Create `client_accounts` Table

```sql
-- Create client accounts table
CREATE TABLE IF NOT EXISTS client_accounts (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ
);

-- Index for performance (soft delete queries)
CREATE INDEX IF NOT EXISTS idx_client_accounts_deleted_at 
  ON client_accounts(deleted_at);

-- Index for status filtering
CREATE INDEX IF NOT EXISTS idx_client_accounts_status 
  ON client_accounts(status);

COMMENT ON TABLE client_accounts IS 'Client organizations that own security sites';
```

---

### Step 2: Create `sites` Table

```sql
-- Create sites table
CREATE TABLE IF NOT EXISTS sites (
  id BIGSERIAL PRIMARY KEY,
  client_account_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  address VARCHAR(500),
  latitude DECIMAL(10,8),
  longitude DECIMAL(11,8),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  
  -- Foreign key constraint
  CONSTRAINT fk_sites_client_account 
    FOREIGN KEY (client_account_id) 
    REFERENCES client_accounts(id) 
    ON DELETE RESTRICT,
  
  -- Unique constraint: site names unique per client
  CONSTRAINT uq_sites_client_name 
    UNIQUE (client_account_id, name)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_sites_client_account_id 
  ON sites(client_account_id);

CREATE INDEX IF NOT EXISTS idx_sites_deleted_at 
  ON sites(deleted_at);

CREATE INDEX IF NOT EXISTS idx_sites_status 
  ON sites(status);

-- Geolocation index for future proximity queries
CREATE INDEX IF NOT EXISTS idx_sites_location 
  ON sites(latitude, longitude);

COMMENT ON TABLE sites IS 'Physical locations where security guards are deployed';
```

---

### Step 3: Verify Tables Created

```sql
-- Check tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('client_accounts', 'sites')
ORDER BY table_name;

-- Expected output:
-- client_accounts
-- sites

-- Verify foreign key constraint
SELECT
  conname AS constraint_name,
  conrelid::regclass AS table_name,
  confrelid::regclass AS referenced_table
FROM pg_constraint
WHERE conname = 'fk_sites_client_account';

-- Expected output:
-- fk_sites_client_account | sites | client_accounts
```

---

### Step 4: Seed Test Data (Optional)

```sql
-- Insert test client accounts
INSERT INTO client_accounts (name, status) VALUES
  ('Acme Corporation', 'ACTIVE'),
  ('Wayne Enterprises', 'ACTIVE'),
  ('Stark Industries', 'ACTIVE')
ON CONFLICT (name) DO NOTHING;

-- Insert test sites
INSERT INTO sites (client_account_id, name, address, latitude, longitude, status) 
SELECT 
  ca.id,
  'Headquarters',
  '123 Main Street, New York, NY 10001',
  40.7128,
  -74.0060,
  'ACTIVE'
FROM client_accounts ca
WHERE ca.name = 'Acme Corporation'
ON CONFLICT (client_account_id, name) DO NOTHING;

-- Verify seed data
SELECT 
  s.id,
  ca.name AS client_name,
  s.name AS site_name,
  s.address,
  s.latitude,
  s.longitude
FROM sites s
JOIN client_accounts ca ON s.client_account_id = ca.id
WHERE s.deleted_at IS NULL;
```

---

## 🏗️ DEPLOYMENT STEPS

### Step 1: Verify Database Migration

```bash
# Connect to Railway PostgreSQL
railway connect postgres

# Run verification query
SELECT COUNT(*) FROM client_accounts;
SELECT COUNT(*) FROM sites;

# Expected: Tables exist and queries succeed
```

---

### Step 2: Build Application

```bash
cd backend
.\mvnw.cmd clean package -DskipTests
```

**Expected Output**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

**Generated JAR**: `backend/target/sgms-backend-0.0.1-SNAPSHOT.jar`

---

### Step 3: Deploy to Railway

#### Option A: Railway CLI (Recommended)

```bash
# Login to Railway
railway login

# Link to project
railway link

# Deploy
railway up
```

#### Option B: Git Push (Automatic Deployment)

```bash
# Stage changes
git add .

# Commit with descriptive message
git commit -m "feat: implement Site Management module (PHASE 2)

- Add ClientAccountEntity and SiteEntity
- Add DTOs for request/response
- Add repositories with custom queries
- Add services with business logic validation
- Add REST controllers with ADMIN authorization
- Update StartupValidation to check new tables
- Add comprehensive documentation and curl tests"

# Push to Railway-connected branch (usually main)
git push origin main
```

---

### Step 4: Monitor Deployment Logs

```bash
# Watch Railway logs
railway logs

# Look for these success indicators:
# ✓ Database connection successful
# ✓ Database: postgresql://...
# ✓ Validating required database tables...
# ✓ Table exists: users
# ✓ Table exists: guards
# ✓ Table exists: client_accounts
# ✓ Table exists: sites
# ✓ JWT secret configured
# ✓ All startup validations passed
# ✓ SGMS Backend is ready for production traffic
```

---

### Step 5: Verify Deployment

#### Health Check

```bash
curl https://sgms-backend-production.up.railway.app/api/health
```

**Expected Response**:
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL"
      }
    }
  }
}
```

---

#### Authentication Test

```bash
# Login as admin
curl -X POST https://sgms-backend-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@zplusesecurity.com",
    "password": "your-admin-password"
  }'
```

**Expected Response**:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "email": "admin@zplusesecurity.com",
    "fullName": "Admin User",
    "role": "ADMIN"
  },
  "message": null,
  "timestamp": "2026-02-17T..."
}
```

**Extract Token**: Copy the token value for subsequent requests

---

## 🧪 POST-DEPLOYMENT TESTING

### Test 1: Create Client Account

```bash
# Set your JWT token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Create client
curl -X POST https://sgms-backend-production.up.railway.app/api/clients \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Client Inc"
  }'
```

**Expected**: 201 Created with client data

---

### Test 2: Get All Clients

```bash
curl -X GET https://sgms-backend-production.up.railway.app/api/clients \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with array of clients

---

### Test 3: Create Site

```bash
# Use client ID from Test 1 response
curl -X POST https://sgms-backend-production.up.railway.app/api/sites \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientAccountId": 1,
    "name": "Downtown Office",
    "address": "456 Business St, New York, NY",
    "latitude": 40.7589,
    "longitude": -73.9851
  }'
```

**Expected**: 201 Created with site data including client info

---

### Test 4: Get Sites by Client

```bash
curl -X GET "https://sgms-backend-production.up.railway.app/api/sites?clientId=1" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with array of sites for that client

---

### Test 5: Authorization Check (Non-Admin)

```bash
# Login as GUARD or SUPERVISOR (not ADMIN)
# Get their token
GUARD_TOKEN="..."

# Try to access client management (should fail)
curl -X GET https://sgms-backend-production.up.railway.app/api/clients \
  -H "Authorization: Bearer $GUARD_TOKEN"
```

**Expected**: 403 Forbidden

---

## 🔍 TROUBLESHOOTING

### Issue: "Table does not exist"

**Symptoms**: Startup validation fails with "Table 'sites' does not exist"

**Solution**:
1. Verify database migration was run
2. Check Railway PostgreSQL connection
3. Run table creation SQL manually
4. Verify table name is lowercase 'sites' not 'Sites'

---

### Issue: Foreign Key Constraint Violation

**Symptoms**: Cannot create site - "violates foreign key constraint"

**Solution**:
1. Ensure client_accounts table exists
2. Verify client exists before creating site
3. Check client ID is correct in request
4. Query: `SELECT id FROM client_accounts WHERE id = ?`

---

### Issue: Unique Constraint Violation

**Symptoms**: "duplicate key value violates unique constraint"

**Solution**:
1. **Client names must be unique** across all clients
2. **Site names must be unique per client** (not globally)
3. Check existing names: `SELECT name FROM client_accounts`
4. Use different name or update existing record

---

### Issue: 401 Unauthorized

**Symptoms**: All requests return 401 even with token

**Solution**:
1. Verify token is not expired (check JWT expiration)
2. Ensure `Authorization: Bearer TOKEN` header format
3. Check JWT secret matches between login and validation
4. Regenerate token by logging in again

---

### Issue: 403 Forbidden

**Symptoms**: Authenticated user cannot access endpoints

**Solution**:
1. Verify user has ADMIN role
2. Client/Site management is **ADMIN-ONLY**
3. Check user role: Login response includes `"role": "ADMIN"`
4. If not ADMIN, login with admin credentials

---

## 📋 ROLLBACK PLAN

If issues occur during deployment:

### Step 1: Rollback Code

```bash
# Railway: Rollback to previous deployment in dashboard
# OR redeploy previous commit
git revert HEAD
git push origin main
```

---

### Step 2: Database Rollback (If Needed)

```sql
-- CAUTION: Only if absolutely necessary
-- This will delete all client and site data

-- Drop tables in correct order (foreign keys)
DROP TABLE IF EXISTS sites CASCADE;
DROP TABLE IF EXISTS client_accounts CASCADE;
```

**⚠️ WARNING**: This deletes all data. Only use if tables were incorrectly created.

---

## ✅ SUCCESS CRITERIA

Deployment is successful when:

1. ✅ Application starts without errors
2. ✅ Health endpoint returns UP status
3. ✅ StartupValidation passes all checks (users, guards, client_accounts, sites)
4. ✅ Can create client account via POST /api/clients
5. ✅ Can create site via POST /api/sites
6. ✅ Can retrieve clients via GET /api/clients
7. ✅ Can retrieve sites via GET /api/sites
8. ✅ Non-ADMIN users get 403 Forbidden
9. ✅ Invalid client ID returns 400 Bad Request
10. ✅ Duplicate names return 400 Bad Request

---

## 📝 POST-DEPLOYMENT TASKS

### Update Frontend

The frontend (`src/`) can now integrate with:
- `/api/clients` - Client management
- `/api/sites` - Site management

**Next Steps**:
1. Create AdminDashboard client management UI
2. Create site assignment interface
3. Add site selection to guard scheduling
4. Display site information in worker schedules

---

### Documentation

- ✅ `PHASE2_SITE_MANAGEMENT_SUMMARY.md` - Complete implementation details
- ✅ `PHASE2_DEPLOYMENT_GUIDE.md` - This document
- 📄 Update `README.md` with new API endpoints
- 📄 Update `PROJECT_SUMMARY.md` with PHASE 2 completion

---

## 🎯 NEXT PHASE

After successful PHASE 2 deployment, consider:

**PHASE 3 - Guard Site Assignment**
- Assign guards to specific sites
- Track guard schedules per site
- Site check-in/check-out functionality
- Geofencing validation

**PHASE 4 - Client Portal**
- Client-specific logins
- View assigned sites
- View guard assignments
- Activity reports per site

---

**Deployment Guide Complete** ✅  
**Ready to Deploy**: YES  
**Last Updated**: February 2026

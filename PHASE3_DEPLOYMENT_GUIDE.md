# PHASE 3 - Deployment Guide

**Module**: Site Post Management  
**Status**: Ready for Deployment  
**Date**: February 17, 2026

---

## ⚠️ CRITICAL PRE-DEPLOYMENT STEPS

### STEP 1: Execute Database Migration

**YOU MUST run the SQL migration on Railway PostgreSQL BEFORE deploying the code.**

If you deploy without running the migration, the application will fail startup validation and exit.

---

## 📊 DATABASE MIGRATION

### Connect to Railway PostgreSQL

```bash
# Option 1: Railway CLI
railway connect postgres

# Option 2: Direct psql connection
# (Get credentials from Railway dashboard)
psql postgresql://postgres:PASSWORD@HOST.railway.app:5432/railway
```

---

### Execute Migration Script

```bash
# From psql prompt, run the migration file:
\i C:/Zpluse-Security/backend/V2__phase3_site_posts.sql
```

**OR** copy and paste the SQL content from `backend/V2__phase3_site_posts.sql` directly into the psql console.

---

### Verify Tables Created

Run this verification query:

```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('site_posts', 'supervisor_site_mapping', 'client_site_access')
ORDER BY table_name;
```

**Expected Output**:
```
     table_name          
-------------------------
 client_site_access
 site_posts
 supervisor_site_mapping
(3 rows)
```

If all 3 tables appear, migration was successful. ✅

---

### Verify Foreign Keys

```sql
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
```

**Expected Output** (5 constraints):
```
        constraint_name        |       table_name        | referenced_table 
-------------------------------+-------------------------+------------------
 fk_client_site_client         | client_site_access      | users
 fk_client_site_site           | client_site_access      | sites
 fk_site_posts_site            | site_posts              | sites
 fk_supervisor_site_site       | supervisor_site_mapping | sites
 fk_supervisor_site_supervisor | supervisor_site_mapping | users
(5 rows)
```

---

## 🏗️ DEPLOYMENT STEPS

### STEP 2: Build JAR Package

```bash
cd backend
.\mvnw.cmd clean package -DskipTests
```

**Expected Output**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: ~9-10 seconds
[INFO] JAR Created: target/sgms-backend-0.0.1-SNAPSHOT.jar
```

---

### STEP 3: Deploy to Railway

#### Option A: Railway CLI (Recommended)

```bash
# From backend directory
railway up
```

#### Option B: Git Push

```bash
git add .
git commit -m "feat: PHASE 3 - Site Post Management implementation"
git push origin main
```

Railway will automatically detect the push and start deployment.

---

### STEP 4: Monitor Deployment Logs

```bash
railway logs --follow
```

**Critical Success Indicators**:

Look for these log messages:

```
=== SGMS Production Startup Validation ===
Validating database connection...
✓ Database connection successful
  Database: postgresql://...

Validating required database tables...
  ✓ Table 'users' exists
  ✓ Table 'guards' exists
  ✓ Table 'client_accounts' exists
  ✓ Table 'sites' exists
  ✓ Table 'site_posts' exists
  ✓ Table 'supervisor_site_mapping' exists
  ✓ Table 'client_site_access' exists
✓ All required tables exist

Validating JWT secret...
✓ JWT secret is configured

✓ All startup validations passed
✓ SGMS Backend is ready for production traffic
==========================================

Started SgmsBackendApplication in X.XXX seconds
```

---

### STEP 5: Verify Deployment Success

#### Test 1: Health Check

```bash
curl https://sgms-backend-production.up.railway.app/api/health
```

**Expected Response** (200 OK):
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

#### Test 2: Authentication

```bash
curl -X POST https://sgms-backend-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@zplusesecurity.com",
    "password": "your-admin-password"
  }'
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "email": "admin@zplusesecurity.com",
    "fullName": "Admin",
    "role": "ADMIN"
  },
  "message": null,
  "timestamp": "2026-02-17T..."
}
```

**SAVE THE TOKEN** - You'll need it for endpoint testing.

---

## 🧪 POST-DEPLOYMENT TESTING

### Set Token Variable

```bash
# Extract token from login response
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### Test 1: Create Site Post

```bash
curl -X POST https://sgms-backend-production.up.railway.app/api/site-posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "siteId": 1,
    "postName": "Main Entrance",
    "description": "Primary entry point - 24/7 monitoring",
    "requiredGuards": 2
  }'
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "siteId": 1,
    "siteName": "Headquarters",
    "postName": "Main Entrance",
    "description": "Primary entry point - 24/7 monitoring",
    "requiredGuards": 2,
    "status": "ACTIVE",
    "createdAt": "2026-02-17T...",
    "updatedAt": "2026-02-17T...",
    "deletedAt": null
  },
  "message": "Site post created successfully",
  "timestamp": "2026-02-17T..."
}
```

---

### Test 2: Get Posts for Site

```bash
curl -X GET "https://sgms-backend-production.up.railway.app/api/site-posts/site/1" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "siteId": 1,
      "siteName": "Headquarters",
      "postName": "Main Entrance",
      "description": "Primary entry point - 24/7 monitoring",
      "requiredGuards": 2,
      "status": "ACTIVE",
      "createdAt": "2026-02-17T...",
      "updatedAt": "2026-02-17T...",
      "deletedAt": null
    }
  ],
  "message": null,
  "timestamp": "2026-02-17T..."
}
```

---

### Test 3: Assign Supervisor to Site

**Note**: Replace `supervisorUserId` with an actual SUPERVISOR user ID from your database.

```bash
curl -X POST https://sgms-backend-production.up.railway.app/api/supervisor/assign-site \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "supervisorUserId": 5,
    "siteId": 1
  }'
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "supervisorUserId": 5,
    "supervisorName": "John Supervisor",
    "supervisorEmail": "john@example.com",
    "siteId": 1,
    "siteName": "Headquarters",
    "assignedAt": "2026-02-17T...",
    "removedAt": null
  },
  "message": "Supervisor assigned to site successfully",
  "timestamp": "2026-02-17T..."
}
```

---

### Test 4: Get Supervisor's Sites

```bash
curl -X GET https://sgms-backend-production.up.railway.app/api/supervisor/sites/5 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response** (200 OK with array of sites)

---

### Test 5: Grant Client Access

**Note**: Replace `clientUserId` with an actual CLIENT user ID from your database.

```bash
curl -X POST https://sgms-backend-production.up.railway.app/api/client/grant-access \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientUserId": 10,
    "siteId": 1
  }'
```

**Expected Response** (201 Created)

---

### Test 6: Get Client's Sites

```bash
curl -X GET https://sgms-backend-production.up.railway.app/api/client/sites/10 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response** (200 OK with array of sites)

---

### Test 7: Authorization Check (Non-Admin)

Login as a GUARD user and try to access site posts:

```bash
# Login as guard
GUARD_TOKEN="..." # Token from guard login

# Try to access site posts (should fail)
curl -X GET https://sgms-backend-production.up.railway.app/api/site-posts \
  -H "Authorization: Bearer $GUARD_TOKEN"
```

**Expected Response** (403 Forbidden):
```json
{
  "success": false,
  "data": null,
  "message": "Access Denied",
  "timestamp": "2026-02-17T..."
}
```

This confirms authorization is working correctly. ✅

---

## 🔍 TROUBLESHOOTING

### Issue: Startup Validation Fails

**Error Message**:
```
✗ FATAL: Required table 'site_posts' does not exist in database
✗ Application cannot start in unsafe state
```

**Solution**:
1. You forgot to run the database migration
2. Go back to STEP 1 and execute `V2__phase3_site_posts.sql`
3. Verify tables exist with the verification query
4. Redeploy the application

---

### Issue: 400 Bad Request - "Site not found"

**Solution**:
1. Ensure you have at least one site created from PHASE 2
2. Use a valid `siteId` from the database
3. Query existing sites:
   ```bash
   curl -X GET https://sgms-backend-production.up.railway.app/api/sites \
     -H "Authorization: Bearer $TOKEN"
   ```

---

### Issue: 400 Bad Request - "Post name already exists"

**Solution**:
- Post names must be unique per site
- Use a different post name
- Or delete the existing post first
- Or update the existing post instead of creating a new one

---

### Issue: 401 Unauthorized

**Solution**:
1. Token may have expired (check JWT expiration time)
2. Login again to get a fresh token
3. Ensure `Authorization: Bearer TOKEN` header is correct
4. No extra spaces or line breaks in token

---

### Issue: 403 Forbidden

**Solution**:
1. User doesn't have required role
2. Site Posts: Requires ADMIN or SUPERVISOR
3. Supervisor Assignment: Requires ADMIN only
4. Client Access: Requires ADMIN only
5. Login with correct user role

---

## ✅ DEPLOYMENT SUCCESS CHECKLIST

After deployment, verify:

- [✅] Application starts without errors
- [✅] Health endpoint returns UP
- [✅] All 7 tables validated in startup logs
- [✅] Can create site post
- [✅] Can retrieve site posts
- [✅] Can assign supervisor to site
- [✅] Can grant client access
- [✅] Authorization working (403 for non-admin)
- [✅] No compilation errors
- [✅] No runtime exceptions in logs

---

## 📊 ENDPOINTS ADDED (Summary)

### Site Posts (6 endpoints)
```
POST   /api/site-posts              - Create post
GET    /api/site-posts              - List all posts
GET    /api/site-posts/{id}         - Get post by ID
GET    /api/site-posts/site/{siteId} - Get posts for site
PUT    /api/site-posts/{id}         - Update post
DELETE /api/site-posts/{id}         - Delete post
```

### Supervisor Management (4 endpoints)
```
POST   /api/supervisor/assign-site              - Assign supervisor
GET    /api/supervisor/sites/{supervisorUserId} - Get supervisor's sites
GET    /api/supervisor/site/{siteId}/supervisors - Get site supervisors
DELETE /api/supervisor/remove-site/{supervisorUserId}/{siteId} - Remove
```

### Client Access (4 endpoints)
```
POST   /api/client/grant-access              - Grant access
GET    /api/client/sites/{clientUserId}      - Get client's sites
GET    /api/client/site/{siteId}/clients     - Get site clients
DELETE /api/client/revoke-access/{clientUserId}/{siteId} - Revoke
```

**Total New Endpoints**: 14

---

## 🎯 NEXT PHASE

After successful PHASE 3 deployment:

**PHASE 4 - Guard Post Assignment**
- Assign guards to site posts
- Track guard locations
- Reassignment workflows
- Assignment history

---

**Deployment Guide Complete** ✅  
**Follow steps carefully to ensure successful deployment**  
**Last Updated**: February 17, 2026

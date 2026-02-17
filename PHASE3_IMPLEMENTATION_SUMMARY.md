# PHASE 3 - Site Post Management

**Implementation Date**: February 17, 2026  
**Status**: ✅ COMPLETE - Ready for Deployment  
**Module**: Site Post Management & Access Control

---

## 📋 SUMMARY

PHASE 3 implements the site post management system, allowing administrators and supervisors to:
1. Create duty posts within sites (e.g., "Main Gate", "Lobby")
2. Assign supervisors to oversee specific sites
3. Grant client users access to view specific sites

---

## 🗄️ DATABASE TABLES CREATED

### 1. site_posts
Represents guard duty stations within a site.

```sql
CREATE TABLE site_posts (
  id BIGSERIAL PRIMARY KEY,
  site_id BIGINT NOT NULL,
  post_name VARCHAR(255) NOT NULL,
  description TEXT,
  required_guards INT NOT NULL DEFAULT 1,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  
  CONSTRAINT fk_site_posts_site FOREIGN KEY (site_id) REFERENCES sites(id),
  CONSTRAINT uq_site_posts_site_post_name UNIQUE (site_id, post_name)
);
```

**Columns**:
- `id`: Auto-incrementing primary key
- `site_id`: Foreign key to sites table
- `post_name`: Name of the post (unique per site)
- `description`: Optional description
- `required_guards`: Number of guards needed at this post
- `status`: ACTIVE, INACTIVE, CLOSED
- `created_at`, `updated_at`, `deleted_at`: Timestamps

---

### 2. supervisor_site_mapping
Maps supervisors to sites they oversee.

```sql
CREATE TABLE supervisor_site_mapping (
  id BIGSERIAL PRIMARY KEY,
  supervisor_user_id BIGINT NOT NULL,
  site_id BIGINT NOT NULL,
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  removed_at TIMESTAMPTZ,
  
  CONSTRAINT fk_supervisor_site_supervisor FOREIGN KEY (supervisor_user_id) REFERENCES users(id),
  CONSTRAINT fk_supervisor_site_site FOREIGN KEY (site_id) REFERENCES sites(id),
  CONSTRAINT uq_supervisor_site_active UNIQUE (supervisor_user_id, site_id)
);
```

**Columns**:
- `id`: Auto-incrementing primary key
- `supervisor_user_id`: Foreign key to users table
- `site_id`: Foreign key to sites table
- `assigned_at`: When assignment was created
- `removed_at`: NULL = active, NOT NULL = removed

---

### 3. client_site_access
Grants client users access to specific sites.

```sql
CREATE TABLE client_site_access (
  id BIGSERIAL PRIMARY KEY,
  client_user_id BIGINT NOT NULL,
  site_id BIGINT NOT NULL,
  granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  revoked_at TIMESTAMPTZ,
  
  CONSTRAINT fk_client_site_client FOREIGN KEY (client_user_id) REFERENCES users(id),
  CONSTRAINT fk_client_site_site FOREIGN KEY (site_id) REFERENCES sites(id),
  CONSTRAINT uq_client_site_active UNIQUE (client_user_id, site_id)
);
```

**Columns**:
- `id`: Auto-incrementing primary key
- `client_user_id`: Foreign key to users table
- `site_id`: Foreign key to sites table
- `granted_at`: When access was granted
- `revoked_at`: NULL = active, NOT NULL = revoked

---

## 📁 FILES CREATED

### Entities (3 files)
```
backend/src/main/java/com/sgms/site/
  ├── SitePostEntity.java
  ├── SupervisorSiteMappingEntity.java
  └── ClientSiteAccessEntity.java
```

### DTOs (7 files)
```
backend/src/main/java/com/sgms/site/dto/
  ├── CreateSitePostRequest.java
  ├── UpdateSitePostRequest.java
  ├── SitePostResponse.java
  ├── AssignSupervisorRequest.java
  ├── SupervisorSiteResponse.java
  ├── GrantClientAccessRequest.java
  └── ClientSiteAccessResponse.java
```

### Repositories (3 files)
```
backend/src/main/java/com/sgms/site/
  ├── SitePostRepository.java
  ├── SupervisorSiteMappingRepository.java
  └── ClientSiteAccessRepository.java
```

### Services (3 files)
```
backend/src/main/java/com/sgms/site/
  ├── SitePostService.java
  ├── SupervisorSiteService.java
  └── ClientSiteAccessService.java
```

### Controllers (3 files)
```
backend/src/main/java/com/sgms/site/
  ├── SitePostController.java
  ├── SupervisorController.java
  └── ClientAccessController.java
```

### Migration Scripts
```
backend/V2__phase3_site_posts.sql
```

---

## 🔌 API ENDPOINTS

### Site Post Management

#### 1. Create Site Post
```
POST /api/site-posts
Authorization: ADMIN, SUPERVISOR
```

**Request Body**:
```json
{
  "siteId": 1,
  "postName": "Main Entrance",
  "description": "Primary entry point - 24/7 monitoring required",
  "requiredGuards": 2
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "siteId": 1,
    "siteName": "Headquarters",
    "postName": "Main Entrance",
    "description": "Primary entry point - 24/7 monitoring required",
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

#### 2. Get All Site Posts
```
GET /api/site-posts
Authorization: ADMIN, SUPERVISOR
```

#### 3. Get Site Post by ID
```
GET /api/site-posts/{id}
Authorization: ADMIN, SUPERVISOR
```

#### 4. Get Posts by Site ID
```
GET /api/site-posts/site/{siteId}
Authorization: ADMIN, SUPERVISOR
```

#### 5. Update Site Post
```
PUT /api/site-posts/{id}
Authorization: ADMIN, SUPERVISOR
```

**Request Body**:
```json
{
  "postName": "Main Entrance Gate",
  "description": "Updated description",
  "requiredGuards": 3,
  "status": "ACTIVE"
}
```

#### 6. Delete Site Post
```
DELETE /api/site-posts/{id}
Authorization: ADMIN
```

---

### Supervisor Site Assignment

#### 1. Assign Supervisor to Site
```
POST /api/supervisor/assign-site
Authorization: ADMIN
```

**Request Body**:
```json
{
  "supervisorUserId": 5,
  "siteId": 1
}
```

**Response** (201 Created):
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

#### 2. Get Sites for Supervisor
```
GET /api/supervisor/sites/{supervisorUserId}
Authorization: ADMIN, SUPERVISOR
```

#### 3. Get Supervisors for Site
```
GET /api/supervisor/site/{siteId}/supervisors
Authorization: ADMIN
```

#### 4. Remove Supervisor from Site
```
DELETE /api/supervisor/remove-site/{supervisorUserId}/{siteId}
Authorization: ADMIN
```

---

### Client Site Access

#### 1. Grant Client Access to Site
```
POST /api/client/grant-access
Authorization: ADMIN
```

**Request Body**:
```json
{
  "clientUserId": 10,
  "siteId": 1
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "clientUserId": 10,
    "clientName": "Jane Client",
    "clientEmail": "jane@client.com",
    "siteId": 1,
    "siteName": "Headquarters",
    "grantedAt": "2026-02-17T...",
    "revokedAt": null
  },
  "message": "Client access granted successfully",
  "timestamp": "2026-02-17T..."
}
```

#### 2. Get Sites for Client
```
GET /api/client/sites/{clientUserId}
Authorization: ADMIN, CLIENT
```

#### 3. Get Clients for Site
```
GET /api/client/site/{siteId}/clients
Authorization: ADMIN
```

#### 4. Revoke Client Access
```
DELETE /api/client/revoke-access/{clientUserId}/{siteId}
Authorization: ADMIN
```

---

## 🔒 AUTHORIZATION MATRIX

| Endpoint | ADMIN | SUPERVISOR | GUARD | CLIENT |
|----------|-------|------------|-------|--------|
| Create Site Post | ✅ | ✅ | ❌ | ❌ |
| View Site Posts | ✅ | ✅ | ❌ | ❌ |
| Update Site Post | ✅ | ✅ | ❌ | ❌ |
| Delete Site Post | ✅ | ❌ | ❌ | ❌ |
| Assign Supervisor | ✅ | ❌ | ❌ | ❌ |
| View Supervisor Sites | ✅ | ✅ (own) | ❌ | ❌ |
| Remove Supervisor | ✅ | ❌ | ❌ | ❌ |
| Grant Client Access | ✅ | ❌ | ❌ | ❌ |
| View Client Sites | ✅ | ❌ | ❌ | ✅ (own) |
| Revoke Client Access | ✅ | ❌ | ❌ | ❌ |

---

## 🧪 TESTING WITH CURL

### Prerequisites

```bash
# 1. Login as admin
curl -X POST https://sgms-backend-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@zplusesecurity.com",
    "password": "your-admin-password"
  }'

# Extract token from response
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
    "description": "Primary entry point",
    "requiredGuards": 2
  }'
```

**Expected**: 201 Created with post data

---

### Test 2: Get Posts for Site

```bash
curl -X GET https://sgms-backend-production.up.railway.app/api/site-posts/site/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with array of posts

---

### Test 3: Assign Supervisor to Site

```bash
curl -X POST https://sgms-backend-production.up.railway.app/api/supervisor/assign-site \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "supervisorUserId": 5,
    "siteId": 1
  }'
```

**Expected**: 201 Created with assignment data

---

### Test 4: Get Supervisor's Sites

```bash
curl -X GET https://sgms-backend-production.up.railway.app/api/supervisor/sites/5 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with array of sites

---

### Test 5: Grant Client Access

```bash
curl -X POST https://sgms-backend-production.up.railway.app/api/client/grant-access \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientUserId": 10,
    "siteId": 1
  }'
```

**Expected**: 201 Created with access data

---

### Test 6: Get Client's Sites

```bash
curl -X GET https://sgms-backend-production.up.railway.app/api/client/sites/10 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with array of accessible sites

---

## 🚀 DEPLOYMENT INSTRUCTIONS

### Step 1: Execute Database Migration

**CRITICAL**: Run this SQL migration on Railway PostgreSQL **BEFORE** deploying the code.

```bash
# Connect to Railway PostgreSQL
railway connect postgres

# OR use psql
psql postgresql://postgres:password@host.railway.app:5432/railway
```

Execute the migration script:
```bash
\i backend/V2__phase3_site_posts.sql
```

Or copy-paste the SQL content from `V2__phase3_site_posts.sql`.

---

### Step 2: Verify Tables Created

```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('site_posts', 'supervisor_site_mapping', 'client_site_access')
ORDER BY table_name;
```

**Expected Output**:
```
client_site_access
site_posts
supervisor_site_mapping
```

---

### Step 3: Deploy to Railway

#### Option A: Railway CLI

```bash
cd backend
railway up
```

#### Option B: Git Push

```bash
git add .
git commit -m "feat: implement PHASE 3 - Site Post Management"
git push origin main
```

---

### Step 4: Monitor Deployment

```bash
railway logs
```

**Look for**:
```
✓ Database connection successful
✓ Table 'users' exists
✓ Table 'guards' exists
✓ Table 'client_accounts' exists
✓ Table 'sites' exists
✓ Table 'site_posts' exists
✓ Table 'supervisor_site_mapping' exists
✓ Table 'client_site_access' exists
✓ All startup validations passed
✓ SGMS Backend is ready for production traffic
```

---

### Step 5: Verify Endpoints

```bash
# Health check
curl https://sgms-backend-production.up.railway.app/api/health

# Test authentication
curl -X POST https://sgms-backend-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@zplusesecurity.com",
    "password": "your-password"
  }'
```

---

## 📊 COMPILATION STATUS

```
Maven Build: ✅ BUILD SUCCESS
Total Files: 64 source files compiled
JAR Created: sgms-backend-0.0.1-SNAPSHOT.jar
Build Time: 9.359 s
```

---

## ✅ COMPLETION CHECKLIST

- [✅] SQL migration script created
- [✅] 3 entities created (SitePostEntity, SupervisorSiteMappingEntity, ClientSiteAccessEntity)
- [✅] 7 DTOs created
- [✅] 3 repositories created
- [✅] 3 services created with business logic
- [✅] 3 controllers created with authorization
- [✅] StartupValidation updated to check new tables
- [✅] Project compiles successfully
- [✅] JAR package built successfully
- [✅] Documentation complete
- [ ] Database migration executed on Railway
- [ ] Deployment to Railway successful
- [ ] Endpoints tested in production

---

## 🎯 NEXT STEPS

After successful PHASE 3 deployment:

**PHASE 4 - Guard Post Assignment**
- Assign guards to specific site posts
- Track guard assignments
- Reassignment functionality
- Assignment history

See [ROADMAP.md](../../docs/ROADMAP.md) for full project roadmap.

---

## 📝 NOTES

### Business Rules Implemented

1. **Site Posts**:
   - Post names must be unique per site
   - Required guards must be at least 1
   - Soft delete supported (deletedAt timestamp)

2. **Supervisor Site Mapping**:
   - One supervisor can be assigned to multiple sites
   - Cannot assign same supervisor to same site twice (active)
   - Removal sets removedAt timestamp (soft delete)

3. **Client Site Access**:
   - One client can have access to multiple sites
   - Cannot grant duplicate access to same client/site (active)
   - Revocation sets revokedAt timestamp (soft delete)

### Validation Rules

- All foreign keys validated in service layer
- Unique constraints enforced at database and service level
- All inputs validated using Jakarta Validation annotations
- Proper exception handling with meaningful error messages

---

**PHASE 3 COMPLETE** ✅  
**Ready for Production Deployment**  
**Date**: February 17, 2026

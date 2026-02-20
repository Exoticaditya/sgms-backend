# PHASE A - Guard Deployment API Testing Guide

## ✅ IMPLEMENTATION COMPLETE

**Date**: February 18, 2026
**Module**: Guard Deployment (Operations Core)
**Status**: Ready for Testing

---

## 📋 Prerequisites

Before testing, ensure:
1. ✅ Database migration V3 executed on Railway PostgreSQL
2. ✅ Backend application running (Spring Boot)
3. ✅ You have ADMIN or SUPERVISOR credentials
4. ✅ At least one guard exists in system
5. ✅ At least one site post exists in system

---

## 🔑 Authentication

All assignment endpoints require ADMIN or SUPERVISOR role.

**Step 1: Login to get JWT token**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@zplusesecurity.com",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "email": "admin@zplusesecurity.com",
      "roles": ["ADMIN"]
    }
  },
  "timestamp": "2026-02-18T10:00:00Z"
}
```

**Save the token:**
```bash
export TOKEN="eyJhbGciOiJIUzI1NiIs..."
```

---

## 🧪 API Tests

### 1. Get All Shift Types

**Purpose**: Retrieve shift type options for dropdown (DAY, NIGHT, EVENING)

```bash
curl -X GET http://localhost:8080/api/assignments/shift-types \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "DAY",
      "startTime": "06:00:00",
      "endTime": "14:00:00",
      "description": "Day shift - morning to afternoon",
      "createdAt": "2026-02-18T10:00:00Z"
    },
    {
      "id": 2,
      "name": "EVENING",
      "startTime": "14:00:00",
      "endTime": "22:00:00",
      "description": "Evening shift - afternoon to night",
      "createdAt": "2026-02-18T10:00:00Z"
    },
    {
      "id": 3,
      "name": "NIGHT",
      "startTime": "22:00:00",
      "endTime": "06:00:00",
      "description": "Night shift - night to morning",
      "createdAt": "2026-02-18T10:00:00Z"
    }
  ],
  "message": null,
  "timestamp": "2026-02-18T10:05:00Z"
}
```

---

### 2. Create Guard Assignment

**Purpose**: Assign a guard to a site post with shift and effective dates

**Prerequisites:**
- Get a guard ID: `GET /api/guards`
- Get a site post ID: `GET /api/site-posts`
- Get a shift type ID: `GET /api/assignments/shift-types`

```bash
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "sitePostId": 1,
    "shiftTypeId": 1,
    "effectiveFrom": "2026-03-01",
    "effectiveTo": "2026-12-31",
    "notes": "Main gate assignment - Day shift"
  }'
```

**Expected Response (HTTP 201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "guardId": 1,
    "guardEmployeeCode": "G001",
    "guardName": "John Doe",
    "sitePostId": 1,
    "sitePostName": "Main Gate",
    "siteId": 1,
    "siteName": "Headquarters",
    "clientId": 1,
    "clientName": "ABC Corporation",
    "shiftTypeId": 1,
    "shiftTypeName": "DAY",
    "shiftStartTime": "06:00:00",
    "shiftEndTime": "14:00:00",
    "effectiveFrom": "2026-03-01",
    "effectiveTo": "2026-12-31",
    "status": "ACTIVE",
    "notes": "Main gate assignment - Day shift",
    "createdAt": "2026-02-18T10:10:00Z",
    "updatedAt": "2026-02-18T10:10:00Z",
    "createdByUserId": 1,
    "createdByEmail": "admin@zplusesecurity.com"
  },
  "message": null,
  "timestamp": "2026-02-18T10:10:00Z"
}
```

**Validation Tests:**

**Test: Missing required field**
```bash
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "sitePostId": 1
  }'
```
Expected: HTTP 400 Bad Request - "Shift type ID is required"

**Test: Invalid date range**
```bash
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "sitePostId": 1,
    "shiftTypeId": 1,
    "effectiveFrom": "2026-12-31",
    "effectiveTo": "2026-01-01"
  }'
```
Expected: HTTP 400 Bad Request - "Effective to date must be on or after effective from date"

**Test: Overlapping assignment**
```bash
# First create an assignment for 2026-03-01 to 2026-12-31
# Then try to create another for the same guard with overlapping dates
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "sitePostId": 2,
    "shiftTypeId": 1,
    "effectiveFrom": "2026-06-01",
    "effectiveTo": "2026-09-30"
  }'
```
Expected: HTTP 400 Bad Request - "Guard already has an active assignment during this period"

**Test: Ongoing assignment (no end date)**
```bash
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 2,
    "sitePostId": 1,
    "shiftTypeId": 2,
    "effectiveFrom": "2026-03-01",
    "notes": "Permanent assignment - no end date"
  }'
```
Expected: HTTP 201 Created - `effectiveTo` will be null (ongoing)

---

### 3. Get Assignments by Guard ID

**Purpose**: View all assignments for a specific guard (history + active)

```bash
curl -X GET http://localhost:8080/api/assignments/guard/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "guardId": 1,
      "guardEmployeeCode": "G001",
      "guardName": "John Doe",
      "sitePostId": 1,
      "sitePostName": "Main Gate",
      "siteName": "Headquarters",
      "clientName": "ABC Corporation",
      "shiftTypeName": "DAY",
      "shiftStartTime": "06:00:00",
      "shiftEndTime": "14:00:00",
      "effectiveFrom": "2026-03-01",
      "effectiveTo": "2026-12-31",
      "status": "ACTIVE",
      "notes": "Main gate assignment - Day shift",
      "createdAt": "2026-02-18T10:10:00Z"
    }
  ],
  "message": null,
  "timestamp": "2026-02-18T10:15:00Z"
}
```

**Test: Guard with no assignments**
```bash
curl -X GET http://localhost:8080/api/assignments/guard/999 \
  -H "Authorization: Bearer $TOKEN"
```
Expected: HTTP 404 Not Found - "Guard not found with id: 999"

---

### 4. Get Assignments by Site Post ID

**Purpose**: View all guards assigned to a specific site post

```bash
curl -X GET http://localhost:8080/api/assignments/site-post/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "guardId": 1,
      "guardEmployeeCode": "G001",
      "guardName": "John Doe",
      "sitePostName": "Main Gate",
      "shiftTypeName": "DAY",
      "effectiveFrom": "2026-03-01",
      "effectiveTo": "2026-12-31",
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "guardId": 2,
      "guardEmployeeCode": "G002",
      "guardName": "Jane Smith",
      "sitePostName": "Main Gate",
      "shiftTypeName": "NIGHT",
      "effectiveFrom": "2026-03-01",
      "effectiveTo": null,
      "status": "ACTIVE"
    }
  ],
  "message": null,
  "timestamp": "2026-02-18T10:20:00Z"
}
```

---

### 5. Get All Active Assignments

**Purpose**: View all currently active assignments in the system

```bash
curl -X GET http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "guardName": "John Doe",
      "sitePostName": "Main Gate",
      "siteName": "Headquarters",
      "clientName": "ABC Corporation",
      "shiftTypeName": "DAY",
      "effectiveFrom": "2026-03-01",
      "effectiveTo": "2026-12-31",
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "guardName": "Jane Smith",
      "sitePostName": "Lobby",
      "siteName": "Downtown Office",
      "clientName": "XYZ Inc",
      "shiftTypeName": "EVENING",
      "effectiveFrom": "2026-03-01",
      "status": "ACTIVE"
    }
  ],
  "message": null,
  "timestamp": "2026-02-18T10:25:00Z"
}
```

---

### 6. Get Assignment by ID

**Purpose**: Get detailed information about a specific assignment

```bash
curl -X GET http://localhost:8080/api/assignments/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "guardId": 1,
    "guardEmployeeCode": "G001",
    "guardName": "John Doe",
    "sitePostId": 1,
    "sitePostName": "Main Gate",
    "siteId": 1,
    "siteName": "Headquarters",
    "clientId": 1,
    "clientName": "ABC Corporation",
    "shiftTypeId": 1,
    "shiftTypeName": "DAY",
    "shiftStartTime": "06:00:00",
    "shiftEndTime": "14:00:00",
    "effectiveFrom": "2026-03-01",
    "effectiveTo": "2026-12-31",
    "status": "ACTIVE",
    "notes": "Main gate assignment - Day shift",
    "createdAt": "2026-02-18T10:10:00Z",
    "updatedAt": "2026-02-18T10:10:00Z",
    "createdByUserId": 1,
    "createdByEmail": "admin@zplusesecurity.com"
  },
  "message": null,
  "timestamp": "2026-02-18T10:30:00Z"
}
```

---

### 7. Cancel Assignment

**Purpose**: Cancel/end a guard assignment (sets status to CANCELLED)

```bash
curl -X DELETE http://localhost:8080/api/assignments/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (HTTP 204 No Content):**
No response body (successful deletion)

**Verify cancellation:**
```bash
curl -X GET http://localhost:8080/api/assignments/1 \
  -H "Authorization: Bearer $TOKEN"
```
Response should show: `"status": "CANCELLED"`

**Test: Delete non-existent assignment**
```bash
curl -X DELETE http://localhost:8080/api/assignments/999 \
  -H "Authorization: Bearer $TOKEN"
```
Expected: HTTP 404 Not Found - "Assignment not found with id: 999"

---

## 🔐 Authorization Tests

**Test: Unauthenticated request**
```bash
curl -X GET http://localhost:8080/api/assignments
```
Expected: HTTP 401 Unauthorized

**Test: GUARD role access (should fail)**
```bash
# Login as GUARD user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "guard@example.com",
    "password": "password"
  }'

# Try to access assignments
curl -X GET http://localhost:8080/api/assignments \
  -H "Authorization: Bearer <GUARD_TOKEN>"
```
Expected: HTTP 403 Forbidden

**Test: CLIENT role access (should fail)**
```bash
# Login as CLIENT user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@example.com",
    "password": "password"
  }'

# Try to access assignments
curl -X GET http://localhost:8080/api/assignments \
  -H "Authorization: Bearer <CLIENT_TOKEN>"
```
Expected: HTTP 403 Forbidden

---

## 📊 Business Logic Tests

### Scenario 1: Complete Guard Deployment Flow

```bash
# Step 1: Get available guards
curl -X GET http://localhost:8080/api/guards \
  -H "Authorization: Bearer $TOKEN"

# Step 2: Get available site posts
curl -X GET http://localhost:8080/api/site-posts \
  -H "Authorization: Bearer $TOKEN"

# Step 3: Get shift types
curl -X GET http://localhost:8080/api/assignments/shift-types \
  -H "Authorization: Bearer $TOKEN"

# Step 4: Create DAY shift assignment
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "sitePostId": 1,
    "shiftTypeId": 1,
    "effectiveFrom": "2026-03-01",
    "notes": "Day shift - Main gate"
  }'

# Step 5: Create NIGHT shift assignment (different guard, same post)
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 2,
    "sitePostId": 1,
    "shiftTypeId": 3,
    "effectiveFrom": "2026-03-01",
    "notes": "Night shift - Main gate"
  }'

# Step 6: Verify both assignments at the site post
curl -X GET http://localhost:8080/api/assignments/site-post/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Scenario 2: Guard Rotation

```bash
# Assign Guard 1 to Post A (March to June)
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "sitePostId": 1,
    "shiftTypeId": 1,
    "effectiveFrom": "2026-03-01",
    "effectiveTo": "2026-06-30",
    "notes": "Q1-Q2 assignment"
  }'

# Assign Guard 1 to Post B (July onwards) - rotation
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "sitePostId": 2,
    "shiftTypeId": 1,
    "effectiveFrom": "2026-07-01",
    "notes": "Q3-Q4 assignment"
  }'

# View guard's assignment history
curl -X GET http://localhost:8080/api/assignments/guard/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## ✅ Success Criteria

### Database Verification

Connect to Railway PostgreSQL and run:

```sql
-- Verify shift types seeded
SELECT * FROM shift_types ORDER BY start_time;

-- Verify assignments created
SELECT 
  ga.id,
  g.employee_code,
  sp.post_name,
  st.name AS shift_type,
  ga.effective_from,
  ga.effective_to,
  ga.status
FROM guard_assignments ga
  JOIN guards g ON ga.guard_id = g.id
  JOIN site_posts sp ON ga.site_post_id = sp.id
  JOIN shift_types st ON ga.shift_type_id = st.id
ORDER BY ga.created_at DESC;

-- Use the helper view
SELECT * FROM v_active_guard_assignments;
```

### Application Verification

```bash
# Check application started without errors
# Look for these log messages:
# ✅ "Mapped \"{[/api/assignments]...\" to GuardAssignmentController"
# ✅ "Mapped \"{[/api/assignments/shift-types]...\" to GuardAssignmentController"
```

---

## 🐛 Troubleshooting

### Issue: "Guard not found or inactive"
**Solution**: Ensure guard exists and `deleted_at` is NULL and `status` is 'ACTIVE'

### Issue: "Site post not found or inactive"
**Solution**: Ensure site post exists and `deleted_at` is NULL

### Issue: "Overlapping assignment" error
**Solution**: Check existing assignments for the guard:
```sql
SELECT * FROM guard_assignments 
WHERE guard_id = ? 
  AND status = 'ACTIVE'
ORDER BY effective_from;
```

### Issue: "Current user not found"
**Solution**: Ensure JWT token is valid and user exists in database

---

## 📝 Next Steps

After Phase A completion:
- ✅ Phase A complete
- ⏳ Phase B - Shift Scheduling (weekly rosters)
- ⏳ Phase C - Attendance Engine (check-in/out, GPS tracking)
- ⏳ Phase D - Leave Management
- ⏳ Phase E - Payroll System
- ⏳ Phase F - Client Billing
- ⏳ Phase G - Inventory & Uniforms
- ⏳ Phase H - Reporting & Analytics

---

**Last Updated**: February 18, 2026

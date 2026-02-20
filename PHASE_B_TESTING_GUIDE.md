# PHASE B - ATTENDANCE TRACKING
## Quick Testing Guide

**Date:** February 18, 2026  
**Status:** ✅ Ready for Testing

---

## Prerequisites

Before testing, ensure:
1. ✅ Phase A completed (guard_assignments, shift_types tables exist)
2. ✅ Database migration V4 executed successfully
3. ✅ Spring Boot application started without errors
4. ✅ You have a valid JWT token for API calls

---

## Database Verification

```sql
-- Verify migration ran
SELECT * FROM flyway_schema_history WHERE version = '4';

-- Verify attendance_logs table exists
\d attendance_logs

-- Verify enum type created
SELECT enum_range(NULL::attendance_status);

-- Verify views exist
\dv v_today_attendance_summary
\dv v_pending_checkins_today
\dv v_pending_checkouts_today

-- Verify indexes
\di idx_attendance_*
```

---

## API Testing with curl

### 1. Guard Check-In (On Time)

**Endpoint:** `POST /api/attendance/check-in`

```bash
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "notes": "On time arrival"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "attendanceId": 1,
    "attendanceDate": "2026-02-18",
    "checkInTime": "2026-02-18T08:00:00Z",
    "checkOutTime": null,
    "status": "PRESENT",
    "lateMinutes": 0,
    "earlyLeaveMinutes": 0,
    "notes": "On time arrival",
    "guardId": 1,
    "guardFirstName": "John",
    "guardLastName": "Doe",
    "guardFullName": "John Doe",
    "employeeCode": "G001",
    "assignmentId": 1,
    "sitePostId": 1,
    "postName": "Main Gate",
    "siteId": 1,
    "siteName": "Corporate HQ",
    "clientId": 1,
    "clientName": "ABC Corporation",
    "shiftName": "DAY",
    "shiftStart": "06:00",
    "shiftEnd": "14:00",
    "createdAt": "2026-02-18T08:00:00Z",
    "updatedAt": "2026-02-18T08:00:00Z"
  }
}
```

### 2. Guard Check-In (Late)

**Scenario:** Guard checks in after shift start time

```bash
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 2,
    "notes": "Traffic delay"
  }'
```

**Expected:** `status: "LATE"`, `lateMinutes: X` (calculated automatically)

### 3. Duplicate Check-In (Should Fail)

```bash
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "error": "Attendance already recorded for today. Cannot check in again."
}
```
**Status Code:** 409 CONFLICT

### 4. Check-In Without Assignment (Should Fail)

```bash
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 999
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "error": "No active assignment found for guard today. Cannot check in."
}
```
**Status Code:** 400 BAD REQUEST

### 5. Guard Check-Out (On Time)

**Endpoint:** `POST /api/attendance/check-out`

```bash
curl -X POST http://localhost:8080/api/attendance/check-out \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 1,
    "notes": "Shift completed successfully"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "attendanceId": 1,
    "checkInTime": "2026-02-18T08:00:00Z",
    "checkOutTime": "2026-02-18T16:00:00Z",
    "status": "PRESENT",
    "lateMinutes": 0,
    "earlyLeaveMinutes": 0,
    "notes": "On time arrival | Checkout: Shift completed successfully"
  }
}
```

### 6. Early Check-Out

**Scenario:** Guard checks out before shift end time

```bash
curl -X POST http://localhost:8080/api/attendance/check-out \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 2,
    "notes": "Emergency leave"
  }'
```

**Expected:** `status: "EARLY_LEAVE"`, `earlyLeaveMinutes: X`

### 7. Check-Out Without Check-In (Should Fail)

```bash
curl -X POST http://localhost:8080/api/attendance/check-out \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "guardId": 3
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "error": "No check-in record found for today. Please check in first."
}
```
**Status Code:** 400 BAD REQUEST

### 8. Get Guard Attendance History

**Endpoint:** `GET /api/attendance/guard/{guardId}`

```bash
curl http://localhost:8080/api/attendance/guard/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "attendanceId": 5,
      "attendanceDate": "2026-02-18",
      "status": "PRESENT",
      ...
    },
    {
      "attendanceId": 4,
      "attendanceDate": "2026-02-17",
      "status": "LATE",
      ...
    }
  ]
}
```

### 9. Get Site Attendance (Today)

**Endpoint:** `GET /api/attendance/site/{siteId}`

```bash
curl http://localhost:8080/api/attendance/site/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** List of all guards' attendance at site 1 for today

### 10. Get Site Attendance (Specific Date)

```bash
curl "http://localhost:8080/api/attendance/site/1?date=2026-02-17" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** List of attendance for site 1 on Feb 17, 2026

### 11. Get Today's Summary

**Endpoint:** `GET /api/attendance/today-summary`

```bash
curl http://localhost:8080/api/attendance/today-summary \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** All attendance records for current date across all sites

### 12. Get Attendance by ID

**Endpoint:** `GET /api/attendance/{id}`

```bash
curl http://localhost:8080/api/attendance/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** Single attendance record with ID 1

---

## Scheduled Jobs Testing

### Test 1: Mark Absent Guards (Runs at 23:59)

**Setup:**
1. Create a guard assignment for today
2. Do NOT check in the guard
3. Wait until 23:59 (or manually trigger scheduler for testing)

**Verification:**
```sql
-- Check attendance_logs for absent guards
SELECT * FROM attendance_logs 
WHERE attendance_date = CURRENT_DATE 
  AND status = 'ABSENT'
  AND check_in_time IS NULL;
```

**Expected:** Attendance record created with status = 'ABSENT'

**Logs:**
```
Starting scheduled job: Mark absent guards
Found X active assignments for today
Successfully marked Y guards as ABSENT
```

### Test 2: Mark Missed Checkouts (Runs every hour)

**Setup:**
1. Guard checks in
2. Do NOT check out
3. Wait until shift end time + 2 hours grace period
4. Scheduler runs (every hour)

**Verification:**
```sql
-- Check for missed checkouts
SELECT * FROM attendance_logs 
WHERE attendance_date = CURRENT_DATE 
  AND status = 'MISSED_CHECKOUT'
  AND check_in_time IS NOT NULL
  AND check_out_time IS NULL;
```

**Expected:** Status updated to 'MISSED_CHECKOUT'

**Logs:**
```
Starting scheduled job: Mark missed checkouts
Found X pending checkouts
Successfully marked Y attendance records as MISSED_CHECKOUT
```

### Test 3: Daily Attendance Report (Runs at 01:00)

**Logs:**
```
Starting scheduled job: Daily attendance report
=== ATTENDANCE SUMMARY FOR 2026-02-17 ===
PRESENT: 45
LATE: 5
EARLY_LEAVE: 2
ABSENT: 3
MISSED_CHECKOUT: 1
TOTAL: 56
================================
```

---

## Frontend Testing

### Access Guard Attendance Panel

1. Navigate to: `http://localhost:5173/guard-attendance` (or your configured route)
2. Login as a GUARD user
3. Verify page loads without errors

### Test Check-In Flow

1. Click **CHECK IN** button
2. Optionally add notes
3. Click confirm
4. Verify:
   - ✅ Success notification appears
   - ✅ Check-in time displays
   - ✅ Status badge shows (PRESENT or LATE)
   - ✅ Site and shift info displays
   - ✅ CHECK IN button becomes disabled
   - ✅ CHECK OUT button becomes enabled

### Test Check-Out Flow

1. Click **CHECK OUT** button
2. Optionally add notes
3. Click confirm
4. Verify:
   - ✅ Success notification appears
   - ✅ Check-out time displays
   - ✅ Final status shows (PRESENT, LATE, or EARLY_LEAVE)
   - ✅ Both buttons become disabled
   - ✅ Early leave minutes display (if applicable)

### Test Error Handling

1. Try to check in twice
   - **Expected:** Error notification "Already checked in"

2. Try to check out without checking in
   - **Expected:** Error notification "Please check in first"

3. Disconnect internet and try check-in
   - **Expected:** Error notification with network error message

### Test Auto-Refresh

1. Check in on one browser tab
2. Open same page in another tab
3. Wait 30 seconds
4. Verify second tab updates automatically

### Test Attendance History

1. Scroll to attendance history table
2. Verify:
   - ✅ Past attendance records display
   - ✅ Dates are formatted correctly
   - ✅ Status badges show correct colors
   - ✅ Check-in/out times display
   - ✅ Table is responsive

---

## Status Badge Colors

| Status | Color | Icon |
|--------|-------|------|
| PRESENT | Green | CheckCircle |
| LATE | Yellow | AlertCircle |
| EARLY_LEAVE | Orange | AlertCircle |
| ABSENT | Red | XCircle |
| MISSED_CHECKOUT | Purple | AlertCircle |

---

## Common Test Scenarios

### Scenario 1: Normal Day (Present)
1. Guard checks in at 06:00 (shift starts 06:00)
2. Guard checks out at 14:00 (shift ends 14:00)
3. **Result:** Status = PRESENT, lateMinutes = 0, earlyLeaveMinutes = 0

### Scenario 2: Late Arrival
1. Guard checks in at 07:30 (shift starts 06:00)
2. Guard checks out at 14:00
3. **Result:** Status = LATE, lateMinutes = 90, earlyLeaveMinutes = 0

### Scenario 3: Early Leave
1. Guard checks in at 06:00
2. Guard checks out at 13:00 (shift ends 14:00)
3. **Result:** Status = EARLY_LEAVE, lateMinutes = 0, earlyLeaveMinutes = 60

### Scenario 4: Late + Early Leave
1. Guard checks in at 07:00 (late by 60 min)
2. Guard checks out at 13:00 (early by 60 min)
3. **Result:** Status = LATE (late takes precedence), lateMinutes = 60, earlyLeaveMinutes = 60

### Scenario 5: Absent
1. Guard has active assignment for today
2. Guard never checks in
3. Scheduler runs at 23:59
4. **Result:** Status = ABSENT, no check-in/out times

### Scenario 6: Missed Checkout
1. Guard checks in at 06:00
2. Guard never checks out
3. Shift ends at 14:00, grace period until 16:00
4. Scheduler runs at 17:00
5. **Result:** Status = MISSED_CHECKOUT, check-in time exists, no check-out time

---

## Performance Testing

### Database Query Performance

```sql
-- Test index usage
EXPLAIN ANALYZE 
SELECT * FROM attendance_logs 
WHERE guard_id = 1 
  AND attendance_date >= '2026-01-01';

-- Should use idx_attendance_guard_date_range

-- Test view performance
EXPLAIN ANALYZE 
SELECT * FROM v_today_attendance_summary;

-- Test pending checkouts partial index
EXPLAIN ANALYZE 
SELECT * FROM attendance_logs
WHERE attendance_date = CURRENT_DATE
  AND check_in_time IS NOT NULL
  AND check_out_time IS NULL;

-- Should use idx_attendance_pending_checkout
```

### Load Testing

```bash
# Install Apache Bench
apt-get install apache2-utils

# Test check-in endpoint (100 requests, 10 concurrent)
ab -n 100 -c 10 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -p checkin.json \
  -T "application/json" \
  http://localhost:8080/api/attendance/check-in

# checkin.json content:
# {"guardId": 1, "notes": "Load test"}
```

**Expected:** 
- Average response time < 200ms
- No failed requests
- 50+ requests/second

---

## Troubleshooting

### Issue: "No active assignment found"
**Cause:** Guard doesn't have assignment for today  
**Fix:** Create guard assignment in Phase A:
```bash
curl -X POST http://localhost:8080/api/assignments \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "guardId": 1,
    "sitePostId": 1,
    "shiftTypeId": 1,
    "effectiveFrom": "2026-02-18"
  }'
```

### Issue: Scheduler not running
**Cause:** @EnableScheduling not enabled  
**Fix:** Verify `SgmsBackendApplication.java` has `@EnableScheduling`

### Issue: Late/Early calculations wrong
**Cause:** Timezone mismatch  
**Fix:** Ensure application timezone is UTC (set in main class)

### Issue: Frontend shows outdated data
**Cause:** Auto-refresh not working  
**Fix:** Check browser console for errors, verify API calls succeed

### Issue: "Cannot check in - outside window"
**Cause:** Check-in time outside 2h before/after shift start  
**Fix:** Wait for valid check-in window or adjust shift times in database

---

## Success Criteria

✅ **Database:**
- Migration V4 executes successfully
- All tables, indexes, views created
- Constraints enforced (unique guard+date)

✅ **Backend:**
- Application starts without errors
- All endpoints return valid responses
- Business rules enforced correctly
- Schedulers run at correct times

✅ **Frontend:**
- Guard panel renders without errors
- Check-in/out buttons work
- Status updates in real-time
- Error handling works properly

✅ **Integration:**
- Phase A data (assignments, shifts) accessible
- Guards can check in with active assignments
- Scheduled jobs update database correctly
- Full end-to-end flow works

---

## Next Steps

After successful testing:

1. **Deploy to Production**
   - Run database migration on production DB
   - Deploy backend with @EnableScheduling
   - Deploy frontend with updated routes
   - Monitor scheduler logs

2. **User Training**
   - Train guards on check-in/out process
   - Train supervisors on attendance reports
   - Document standard operating procedures

3. **Phase C Planning**
   - GPS location verification
   - Photo capture at check-in/out
   - Real-time tracking
   - Geofencing alerts

---

## Support

For issues or questions:
1. Check application logs: `backend/logs/`
2. Check scheduler logs for job execution
3. Verify database constraints with `\d attendance_logs`
4. Test API directly with curl/Postman
5. Check browser console for frontend errors

---

**Testing Status:** Ready  
**Go-Live Ready:** Pending successful test execution  
**Phase C:** Awaiting Phase B completion

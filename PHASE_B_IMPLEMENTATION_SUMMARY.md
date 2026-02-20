# PHASE B - ATTENDANCE & DUTY TRACKING ENGINE
## Implementation Summary

**Date:** February 18, 2026  
**Status:** ✅ COMPLETE  
**Version:** 1.0.0

---

## Overview

Phase B implements a complete attendance tracking system for the SGMS ERP platform. Guards can check in and out of their assigned shifts, with automatic status calculation based on shift timing rules. The system includes scheduled jobs for automatic absence and missed checkout detection.

---

## Database Schema

### Migration File
- **File:** `V4__phase_b_attendance_tracking.sql`
- **Location:** `backend/src/main/resources/db/migration/`

### Tables Created

#### 1. `attendance_logs` (Main Table)
```sql
- id (BIGSERIAL PRIMARY KEY)
- guard_id (FK → guards.id)
- assignment_id (FK → guard_assignments.id)
- attendance_date (DATE, NOT NULL)
- check_in_time (TIMESTAMP)
- check_out_time (TIMESTAMP)
- status (attendance_status ENUM)
- late_minutes (INTEGER, DEFAULT 0)
- early_leave_minutes (INTEGER, DEFAULT 0)
- notes (TEXT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

Constraints:
- UNIQUE (guard_id, attendance_date) - One record per guard per day
- CHECK (check_out_time >= check_in_time)
- CHECK (late_minutes >= 0)
- CHECK (early_leave_minutes >= 0)
```

### Enum Types Created

#### `attendance_status`
- `PRESENT` - Guard checked in on time and completed shift
- `LATE` - Guard checked in after shift start
- `EARLY_LEAVE` - Guard checked out before shift end
- `ABSENT` - Guard never checked in (auto-marked)
- `MISSED_CHECKOUT` - Guard checked in but never checked out (auto-marked)

### Indexes Created (8 total)
1. `idx_attendance_guard_id` - Guard-based queries
2. `idx_attendance_assignment_id` - Assignment-based queries
3. `idx_attendance_date` - Date-based queries
4. `idx_attendance_status` - Status filtering
5. `idx_attendance_guard_date_range` - Composite for guard history
6. `idx_attendance_pending_checkout` - Partial index for scheduler
7. `idx_attendance_site_date` - Site attendance reports

### Views Created (3 total)
1. `v_today_attendance_summary` - Denormalized current date attendance
2. `v_pending_checkins_today` - Guards who haven't checked in
3. `v_pending_checkouts_today` - Guards who haven't checked out

---

## Backend Implementation

### Package Structure
```
com.sgms.attendance/
├── AttendanceEntity.java          (JPA entity)
├── AttendanceStatus.java          (Enum)
├── AttendanceRepository.java      (Data access)
├── AttendanceService.java         (Business logic)
├── AttendanceController.java      (REST API)
├── AttendanceScheduler.java       (Scheduled jobs)
└── dto/
    ├── CheckInRequest.java        (Request DTO)
    ├── CheckOutRequest.java       (Request DTO)
    └── AttendanceResponse.java    (Response DTO)
```

### Entities

#### AttendanceEntity
- **File:** `AttendanceEntity.java`
- **Table:** `attendance_logs`
- **Relationships:**
  - `@ManyToOne` → `GuardEntity` (guard_id)
  - `@ManyToOne` → `GuardAssignmentEntity` (assignment_id)
- **Auto-generated:** createdAt, updatedAt
- **Lifecycle Hooks:** @PrePersist, @PreUpdate

#### AttendanceStatus (Enum)
- **Values:** PRESENT, LATE, EARLY_LEAVE, ABSENT, MISSED_CHECKOUT
- **Usage:** Status determination logic documented in enum

### DTOs

#### CheckInRequest
```java
- guardId: Long (required)
- notes: String (optional)
```

#### CheckOutRequest
```java
- guardId: Long (required)
- notes: String (optional)
```

#### AttendanceResponse (Denormalized)
```java
Attendance Fields:
- attendanceId, attendanceDate, checkInTime, checkOutTime
- status, lateMinutes, earlyLeaveMinutes, notes
- createdAt, updatedAt

Guard Details:
- guardId, guardFirstName, guardLastName, guardFullName, employeeCode

Assignment Details:
- assignmentId

Site Post Details:
- sitePostId, postName

Site Details:
- siteId, siteName

Client Details:
- clientId, clientName

Shift Details:
- shiftName, shiftStart, shiftEnd
```

### Repository

#### AttendanceRepository
**Methods:**
1. `findByGuardIdAndDate()` - Find today's attendance for guard
2. `findByGuardId()` - Get all attendance for guard
3. `findByGuardIdAndDateRange()` - Attendance history for date range
4. `findBySiteIdAndDate()` - Site attendance report
5. `findByDate()` - All attendance for specific date
6. `findPendingCheckouts()` - Guards who haven't checked out
7. `existsByGuardIdAndDate()` - Check duplicate check-in
8. `findByStatusAndDate()` - Filter by status
9. `getAttendanceSummaryByDate()` - Status counts
10. `findBySitePostIdAndDate()` - Post-level attendance

**Custom Queries:** All use JPQL for type safety

### Service Layer

#### AttendanceService
**Core Methods:**

1. **checkIn(CheckInRequest)**
   - Validates guard exists and is active
   - Verifies active assignment for today
   - Checks no duplicate check-in
   - Validates check-in time window (2h before to 2h after shift start)
   - Calculates LATE status and late minutes
   - Creates attendance record
   - Returns AttendanceResponse

2. **checkOut(CheckOutRequest)**
   - Validates guard exists
   - Finds today's attendance record
   - Verifies not already checked out
   - Calculates EARLY_LEAVE status and early leave minutes
   - Updates attendance record
   - Appends checkout notes
   - Returns AttendanceResponse

3. **getGuardAttendance(guardId)**
   - Returns full attendance history for guard
   - Ordered by date descending

4. **getSiteAttendance(siteId, date)**
   - Returns attendance for all guards at site on specific date
   - Defaults to today if date not provided

5. **getTodaySummary()**
   - Returns all attendance records for current date
   - Used for dashboard

6. **getAttendanceById(id)**
   - Returns single attendance record

**Business Logic:**
- Check-in window: 2 hours before to 2 hours after shift start
- Late detection: check-in after shift start time
- Early leave detection: check-out before shift end
- Overnight shift handling (e.g., 22:00 - 06:00)
- Status hierarchy: LATE takes precedence over EARLY_LEAVE

### Scheduled Jobs

#### AttendanceScheduler

**Job 1: markAbsentGuards()**
- **Schedule:** Nightly at 11:59 PM (`0 59 23 * * *`)
- **Logic:**
  1. Find all active assignments for today
  2. Check if attendance record exists
  3. Create ABSENT record if no check-in
- **Logging:** Count of guards marked absent

**Job 2: markMissedCheckouts()**
- **Schedule:** Every hour (`0 0 * * * *`)
- **Logic:**
  1. Find attendance records with check-in but no check-out
  2. Check if shift end + 2 hour grace period passed
  3. Mark as MISSED_CHECKOUT if grace period exceeded
- **Logging:** Count of missed checkouts

**Job 3: dailyAttendanceReport()**
- **Schedule:** Daily at 1:00 AM (`0 0 1 * * *`)
- **Purpose:** Log yesterday's attendance statistics
- **Output:** Status breakdown (PRESENT: X, LATE: Y, etc.)

**Configuration:**
- Added `@EnableScheduling` to `SgmsBackendApplication.java`
- Uses Spring Boot's built-in scheduler
- Transactional execution for data integrity

### REST API Endpoints

#### AttendanceController
**Base Path:** `/api/attendance`

**Endpoints:**

1. **POST /api/attendance/check-in**
   - **Auth:** ADMIN, SUPERVISOR, GUARD
   - **Request:** CheckInRequest
   - **Response:** ApiResponse<AttendanceResponse>
   - **Status:** 201 CREATED

2. **POST /api/attendance/check-out**
   - **Auth:** ADMIN, SUPERVISOR, GUARD
   - **Request:** CheckOutRequest
   - **Response:** ApiResponse<AttendanceResponse>
   - **Status:** 200 OK

3. **GET /api/attendance/guard/{guardId}**
   - **Auth:** ADMIN, SUPERVISOR
   - **Response:** ApiResponse<List<AttendanceResponse>>
   - **Description:** Attendance history for guard

4. **GET /api/attendance/site/{siteId}?date=YYYY-MM-DD**
   - **Auth:** ADMIN, SUPERVISOR
   - **Query Params:** date (optional, defaults to today)
   - **Response:** ApiResponse<List<AttendanceResponse>>
   - **Description:** Site attendance report

5. **GET /api/attendance/today-summary**
   - **Auth:** ADMIN, SUPERVISOR
   - **Response:** ApiResponse<List<AttendanceResponse>>
   - **Description:** All attendance for current date

6. **GET /api/attendance/{id}**
   - **Auth:** ADMIN, SUPERVISOR
   - **Response:** ApiResponse<AttendanceResponse>
   - **Description:** Single attendance record

**Security:**
- All endpoints use `@PreAuthorize` annotations
- JWT-based authentication (existing system)
- Role-based access control

---

## Frontend Implementation

### Files Created

#### 1. Attendance Service
- **File:** `src/services/attendanceService.js`
- **Functions:**
  - `checkIn(checkInData)` - Guard check-in
  - `checkOut(checkOutData)` - Guard check-out
  - `getGuardAttendance(guardId)` - Attendance history
  - `getSiteAttendance(siteId, date)` - Site report
  - `getTodaySummary()` - Today's summary
  - `getAttendanceById(id)` - Single record

#### 2. API Configuration Update
- **File:** `src/config/api.js`
- **Added:** ATTENDANCE endpoints object

#### 3. Guard Attendance Panel
- **File:** `src/pages/guards/GuardAttendancePanel.jsx`
- **Features:**
  - Large CHECK IN / CHECK OUT buttons
  - Today's attendance status display
  - Assigned site and shift information
  - Real-time status updates (30-second polling)
  - Notes field for both check-in and check-out
  - Attendance history table
  - Color-coded status badges
  - Responsive layout

**UI Components:**
- Status badges (color-coded by attendance status)
- Time formatting helpers
- Date formatting helpers
- Loading states
- Error notifications
- Success notifications

**User Experience:**
- Disabled buttons when action not allowed
- Clear visual feedback
- Auto-refresh every 30 seconds
- Confirmation messages
- Error handling with user-friendly messages

---

## Business Rules Implementation

### Check-In Rules
✅ Guard must have active assignment for check-in date  
✅ One attendance record per guard per date (DB constraint)  
✅ Check-in window: 2 hours before to 2 hours after shift start  
✅ Auto-mark LATE if checked in after shift start  
✅ Calculate and store late minutes  
✅ Create attendance record with PRESENT or LATE status  

### Check-Out Rules
✅ Must have checked in first  
✅ Cannot check out twice  
✅ Auto-mark EARLY_LEAVE if checked out before shift end  
✅ Calculate and store early leave minutes  
✅ Preserve LATE status even if early leave (late is more severe)  
✅ Append checkout notes to existing notes  

### Automatic Status Updates
✅ ABSENT: Marked by nightly job (23:59) for guards who never checked in  
✅ MISSED_CHECKOUT: Marked by hourly job for guards who didn't check out after shift end + 2h grace  
✅ Scheduled jobs run automatically in background  
✅ Transaction rollback on scheduler errors  

### Data Integrity
✅ Foreign key constraints (guard_id, assignment_id)  
✅ Unique constraint (guard_id, attendance_date)  
✅ Check constraints (checkout >= checkin, minutes >= 0)  
✅ ON DELETE RESTRICT prevents orphaned records  
✅ Automatic timestamp updates (trigger)  

---

## Testing Checklist

### Database Testing
- [ ] Run migration V4 successfully
- [ ] Verify attendance_logs table created
- [ ] Verify attendance_status enum created
- [ ] Verify all 8 indexes exist
- [ ] Verify 3 views exist
- [ ] Test unique constraint (duplicate check-in fails)
- [ ] Test check constraints (invalid times fail)

### Backend Testing
- [ ] Application starts without errors
- [ ] @EnableScheduling annotation active
- [ ] All attendance package files compile
- [ ] Entities map correctly to database
- [ ] Repository queries execute successfully

### API Testing (Use Postman/curl)

#### Check-In Tests
```bash
# Valid check-in
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"guardId": 1, "notes": "On time"}'

# Expected: 201 CREATED, attendance record with PRESENT or LATE status

# Duplicate check-in (should fail)
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"guardId": 1}'

# Expected: 409 CONFLICT

# No active assignment (should fail)
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"guardId": 999}'

# Expected: 400 BAD REQUEST
```

#### Check-Out Tests
```bash
# Valid check-out
curl -X POST http://localhost:8080/api/attendance/check-out \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"guardId": 1, "notes": "Shift completed"}'

# Expected: 200 OK, updated attendance record

# Check-out without check-in (should fail)
curl -X POST http://localhost:8080/api/attendance/check-out \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"guardId": 2}'

# Expected: 400 BAD REQUEST
```

#### Report Tests
```bash
# Guard attendance history
curl http://localhost:8080/api/attendance/guard/1 \
  -H "Authorization: Bearer <JWT_TOKEN>"

# Site attendance (today)
curl http://localhost:8080/api/attendance/site/1 \
  -H "Authorization: Bearer <JWT_TOKEN>"

# Site attendance (specific date)
curl "http://localhost:8080/api/attendance/site/1?date=2026-02-18" \
  -H "Authorization: Bearer <JWT_TOKEN>"

# Today's summary
curl http://localhost:8080/api/attendance/today-summary \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Scheduler Testing
- [ ] markAbsentGuards runs at 23:59
- [ ] markMissedCheckouts runs every hour
- [ ] dailyAttendanceReport runs at 01:00
- [ ] Check logs for scheduler execution
- [ ] Verify absent guards are auto-marked
- [ ] Verify missed checkouts are auto-marked

### Frontend Testing
- [ ] GuardAttendancePanel renders without errors
- [ ] CHECK IN button works when no attendance
- [ ] CHECK OUT button works after check-in
- [ ] Buttons disabled when action not allowed
- [ ] Status badges show correct colors
- [ ] Today's info displays correctly
- [ ] Attendance history table populates
- [ ] Notifications show on success/error
- [ ] Auto-refresh works (30 seconds)

---

## Files Modified/Created

### Backend Files (9 files)

**New Files:**
1. `V4__phase_b_attendance_tracking.sql` - Database migration
2. `AttendanceEntity.java` - JPA entity
3. `AttendanceStatus.java` - Enum
4. `AttendanceRepository.java` - Data access
5. `AttendanceService.java` - Business logic
6. `AttendanceScheduler.java` - Scheduled jobs
7. `AttendanceController.java` - REST API
8. `CheckInRequest.java` - DTO
9. `CheckOutRequest.java` - DTO
10. `AttendanceResponse.java` - DTO

**Modified Files:**
1. `SgmsBackendApplication.java` - Added @EnableScheduling

### Frontend Files (3 files)

**New Files:**
1. `src/services/attendanceService.js` - API service
2. `src/pages/guards/GuardAttendancePanel.jsx` - Guard panel page

**Modified Files:**
1. `src/config/api.js` - Added ATTENDANCE endpoints

---

## Next Steps (Phase C Preview)

Phase B is complete. The next phase would be:

**PHASE C - TRACKING & GPS VERIFICATION**
- GPS coordinates on check-in/out
- Location verification (must be within site radius)
- Real-time guard location tracking
- Geofencing alerts
- Movement history and replay

---

## Deployment Notes

### Database Migration
1. Ensure PostgreSQL is running
2. Flyway will auto-run V4 migration on startup
3. Verify migration success: `SELECT * FROM flyway_schema_history;`

### Application Configuration
- No new environment variables required
- Uses existing Spring Boot configuration
- Scheduler enabled automatically with @EnableScheduling

### Production Checklist
- [ ] Database migration executed
- [ ] Verify shift_types seed data exists (Phase A dependency)
- [ ] Verify guard_assignments table exists (Phase A dependency)
- [ ] Test scheduled jobs run correctly
- [ ] Monitor scheduler logs
- [ ] Configure timezone (UTC recommended)

---

## Dependencies

**Phase A Requirements:**
- `shift_types` table (for shift timing)
- `guard_assignments` table (for active assignments)
- `guards` table (for guard validation)
- `site_posts`, `sites`, `clients` tables (for denormalized responses)

**No New Maven Dependencies Required**
All features use existing Spring Boot dependencies:
- Spring Data JPA
- Spring Boot Starter Web
- Spring Security
- PostgreSQL Driver

---

## Performance Considerations

### Database Indexes
- All foreign keys indexed
- Composite indexes for common queries
- Partial index for scheduler optimization

### Query Optimization
- JPQL queries for type safety
- View materialization for complex joins
- Limited result sets (last 10 in UI)

### Scheduled Jobs
- Runs outside business hours when possible
- Batched updates for efficiency
- Transaction management for data integrity

### Frontend
- Polling interval: 30 seconds (configurable)
- Limited history display (10 records)
- Lazy loading for full history (future enhancement)

---

## Known Limitations & Future Enhancements

### Current Limitations
1. Guard ID currently hardcoded in frontend (needs auth integration)
2. No SMS/email notifications for absence
3. No manager approval workflow for late/early leave
4. No GPS location verification
5. No photo capture at check-in/out
6. No offline mode for mobile app

### Future Enhancements
1. Push notifications for check-in reminders
2. Biometric check-in (fingerprint/face recognition)
3. GPS location tracking and verification
4. Photo capture at check-in/out
5. Manager dashboard for attendance overview
6. Export attendance reports (PDF, Excel)
7. Integration with payroll system
8. Mobile app (React Native)
9. Offline support with sync
10. Real-time dashboard with WebSocket

---

## Support & Troubleshooting

### Common Issues

**Issue:** Check-in fails with "No active assignment"
**Solution:** Verify guard has active assignment for today in guard_assignments table

**Issue:** Scheduler not running
**Solution:** Verify @EnableScheduling is present in main application class

**Issue:** Timezone issues (times showing incorrectly)
**Solution:** Ensure application timezone is UTC, convert in frontend

**Issue:** Late/Early calculations incorrect
**Solution:** Verify shift_types table has correct start_time and end_time

---

## Conclusion

Phase B successfully implements a production-grade attendance tracking system with:
- ✅ Complete database schema with constraints
- ✅ Full backend implementation with business logic
- ✅ Automatic scheduled jobs for status updates
- ✅ REST API with proper security
- ✅ React frontend for guard check-in/out
- ✅ Comprehensive error handling
- ✅ Performance optimization

**Status:** Ready for testing and deployment

**Next Phase:** Phase C - Tracking & GPS Verification

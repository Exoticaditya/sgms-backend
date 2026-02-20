package com.sgms.attendance;

import com.sgms.assignment.GuardAssignmentEntity;
import com.sgms.assignment.GuardAssignmentRepository;
import com.sgms.assignment.ShiftTypeEntity;
import com.sgms.attendance.dto.AttendanceResponse;
import com.sgms.attendance.dto.CheckInRequest;
import com.sgms.attendance.dto.CheckOutRequest;
import com.sgms.guard.GuardEntity;
import com.sgms.guard.GuardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AttendanceService - Core business logic for attendance tracking
 * 
 * Handles:
 * - Guard check-in with validation and late detection
 * - Guard check-out with validation and early leave detection
 * - Attendance record retrieval and reporting
 * - Status calculation and time tracking
 * 
 * Business Rules:
 * - Guard must have active assignment for check-in date
 * - Only one attendance record per guard per date
 * - Check-in window: 2 hours before to 2 hours after shift start
 * - Late: checked in after shift start
 * - Early leave: checked out before shift end
 * - Status auto-updated based on timing
 */
@Service
public class AttendanceService {

  private final AttendanceRepository attendanceRepository;
  private final GuardRepository guardRepository;
  private final GuardAssignmentRepository assignmentRepository;
  private final Clock clock;

  // Check-in window constants
  private static final int CHECK_IN_BEFORE_SHIFT_HOURS = 2;
  private static final int CHECK_IN_AFTER_SHIFT_HOURS = 2;
  private static final int CHECKOUT_GRACE_HOURS = 2;

  public AttendanceService(
      AttendanceRepository attendanceRepository,
      GuardRepository guardRepository,
      GuardAssignmentRepository assignmentRepository,
      Clock clock) {
    this.attendanceRepository = attendanceRepository;
    this.guardRepository = guardRepository;
    this.assignmentRepository = assignmentRepository;
    this.clock = clock;
  }

  /**
   * Process guard check-in
   * 
   * Steps:
   * 1. Validate guard exists and is active
   * 2. Verify guard has active assignment for today
   * 3. Check no existing attendance record for today
   * 4. Determine if guard is late
   * 5. Create attendance record with calculated status and late minutes
   */
  @Transactional
  public AttendanceResponse checkIn(CheckInRequest request) {
    LocalDate today = LocalDate.now(clock);
    Instant now = clock.instant();

    // 1. Validate guard
    GuardEntity guard = guardRepository.findActiveById(request.getGuardId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Guard not found or inactive with id: " + request.getGuardId()));

    // 2. Verify guard has active assignment for today
    List<GuardAssignmentEntity> activeAssignments = assignmentRepository
        .findActiveAssignmentsByGuardId(request.getGuardId(), today);

    if (activeAssignments.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "No active assignment found for guard today. Cannot check in.");
    }

    // Get the first active assignment (guards should have only one active
    // assignment per date)
    GuardAssignmentEntity assignment = activeAssignments.get(0);
    ShiftTypeEntity shift = assignment.getShiftType();

    // 3. Check for existing attendance record
    if (attendanceRepository.existsByGuardIdAndDate(request.getGuardId(), today)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Attendance already recorded for today. Cannot check in again.");
    }

    // 4. Validate check-in time window
    LocalDateTime checkInDateTime = LocalDateTime.ofInstant(now, clock.getZone());
    LocalTime checkInTime = checkInDateTime.toLocalTime();
    LocalTime shiftStart = shift.getStartTime();

    LocalTime earliestCheckIn = shiftStart.minusHours(CHECK_IN_BEFORE_SHIFT_HOURS);
    LocalTime latestCheckIn = shiftStart.plusHours(CHECK_IN_AFTER_SHIFT_HOURS);

    // Handle overnight shifts (e.g., 22:00 - 06:00)
    boolean isOvernight = shift.getStartTime().isAfter(shift.getEndTime());

    if (!isOvernight && (checkInTime.isBefore(earliestCheckIn) || checkInTime.isAfter(latestCheckIn))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          String.format("Check-in window is %s to %s. Current time %s is outside allowed window.",
              earliestCheckIn, latestCheckIn, checkInTime));
    }

    // 5. Determine if late and calculate late minutes
    AttendanceStatus status = AttendanceStatus.PRESENT;
    int lateMinutes = 0;

    if (checkInTime.isAfter(shiftStart)) {
      status = AttendanceStatus.LATE;
      lateMinutes = (int) Duration.between(shiftStart, checkInTime).toMinutes();
    }

    // 6. Create attendance record
    AttendanceEntity attendance = new AttendanceEntity();
    attendance.setGuard(guard);
    attendance.setAssignment(assignment);
    attendance.setAttendanceDate(today);
    attendance.setCheckInTime(now);
    attendance.setStatus(status);
    attendance.setLateMinutes(lateMinutes);
    attendance.setEarlyLeaveMinutes(0);
    attendance.setNotes(request.getNotes());

    AttendanceEntity saved = attendanceRepository.save(attendance);
    return mapToResponse(saved);
  }

  /**
   * Process guard check-out
   * 
   * Steps:
   * 1. Validate guard exists
   * 2. Find today's attendance record (must have checked in)
   * 3. Verify not already checked out
   * 4. Determine if early leave
   * 5. Update attendance record with checkout time and final status
   */
  @Transactional
  public AttendanceResponse checkOut(CheckOutRequest request) {
    LocalDate today = LocalDate.now(clock);
    Instant now = clock.instant();

    // 1. Validate guard exists
    guardRepository.findById(request.getGuardId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Guard not found with id: " + request.getGuardId()));

    // 2. Find attendance record for today
    AttendanceEntity attendance = attendanceRepository.findByGuardIdAndDate(request.getGuardId(), today)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "No check-in record found for today. Please check in first."));

    // 3. Verify not already checked out
    if (attendance.getCheckOutTime() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Already checked out today. Cannot check out again.");
    }

    // 4. Determine if early leave and calculate early leave minutes
    ShiftTypeEntity shift = attendance.getAssignment().getShiftType();
    LocalDateTime checkOutDateTime = LocalDateTime.ofInstant(now, clock.getZone());
    LocalTime checkOutTime = checkOutDateTime.toLocalTime();
    LocalTime shiftEnd = shift.getEndTime();

    int earlyLeaveMinutes = 0;
    AttendanceStatus finalStatus = attendance.getStatus(); // Keep LATE status if was late

    // If guard was PRESENT and checks out early, mark as EARLY_LEAVE
    // If guard was LATE and checks out early, status remains LATE (late is more
    // severe)
    if (checkOutTime.isBefore(shiftEnd)) {
      earlyLeaveMinutes = (int) Duration.between(checkOutTime, shiftEnd).toMinutes();

      // Only change to EARLY_LEAVE if guard was PRESENT (not already LATE)
      if (attendance.getStatus() == AttendanceStatus.PRESENT) {
        finalStatus = AttendanceStatus.EARLY_LEAVE;
      }
    }

    // 5. Update attendance record
    attendance.setCheckOutTime(now);
    attendance.setStatus(finalStatus);
    attendance.setEarlyLeaveMinutes(earlyLeaveMinutes);

    // Append checkout notes if provided
    if (request.getNotes() != null && !request.getNotes().isEmpty()) {
      String existingNotes = attendance.getNotes();
      String combinedNotes = existingNotes != null
          ? existingNotes + " | Checkout: " + request.getNotes()
          : "Checkout: " + request.getNotes();
      attendance.setNotes(combinedNotes);
    }

    AttendanceEntity updated = attendanceRepository.save(attendance);
    return mapToResponse(updated);
  }

  /**
   * Get attendance records for a guard
   */
  @Transactional(readOnly = true)
  public List<AttendanceResponse> getGuardAttendance(Long guardId) {
    // Verify guard exists
    guardRepository.findById(guardId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Guard not found with id: " + guardId));

    return attendanceRepository.findByGuardId(guardId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get attendance records for a site on specific date
   */
  @Transactional(readOnly = true)
  public List<AttendanceResponse> getSiteAttendance(Long siteId, LocalDate date) {
    LocalDate actualDate = date != null ? date : LocalDate.now(clock);
    return attendanceRepository.findBySiteIdAndDate(siteId, actualDate)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get today's attendance summary
   */
  @Transactional(readOnly = true)
  public List<AttendanceResponse> getTodaySummary() {
    return attendanceRepository.findByDate(LocalDate.now(clock))
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get attendance by ID
   */
  @Transactional(readOnly = true)
  public AttendanceResponse getAttendanceById(Long id) {
    AttendanceEntity attendance = attendanceRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Attendance record not found with id: " + id));
    return mapToResponse(attendance);
  }

  /**
   * Map AttendanceEntity to AttendanceResponse DTO
   * Denormalizes all related data for efficient client consumption
   */
  private AttendanceResponse mapToResponse(AttendanceEntity attendance) {
    AttendanceResponse response = new AttendanceResponse();

    // Attendance fields
    response.setAttendanceId(attendance.getId());
    response.setAttendanceDate(attendance.getAttendanceDate());
    response.setCheckInTime(attendance.getCheckInTime());
    response.setCheckOutTime(attendance.getCheckOutTime());
    response.setStatus(attendance.getStatus());
    response.setLateMinutes(attendance.getLateMinutes());
    response.setEarlyLeaveMinutes(attendance.getEarlyLeaveMinutes());
    response.setNotes(attendance.getNotes());
    response.setCreatedAt(attendance.getCreatedAt());
    response.setUpdatedAt(attendance.getUpdatedAt());

    // Guard details
    GuardEntity guard = attendance.getGuard();
    response.setGuardId(guard.getId());
    response.setGuardFirstName(guard.getFirstName());
    response.setGuardLastName(guard.getLastName());
    response.setGuardFullName(guard.getFirstName() + " " +
        (guard.getLastName() != null ? guard.getLastName() : ""));
    response.setEmployeeCode(guard.getEmployeeCode());

    // Assignment details
    GuardAssignmentEntity assignment = attendance.getAssignment();
    response.setAssignmentId(assignment.getId());

    // Site post details
    response.setSitePostId(assignment.getSitePost().getId());
    response.setPostName(assignment.getSitePost().getPostName());

    // Site details
    response.setSiteId(assignment.getSitePost().getSite().getId());
    response.setSiteName(assignment.getSitePost().getSite().getName());

    // Client details
    response.setClientId(assignment.getSitePost().getSite().getClientAccount().getId());
    response.setClientName(assignment.getSitePost().getSite().getClientAccount().getName());

    // Shift details
    ShiftTypeEntity shift = assignment.getShiftType();
    response.setShiftName(shift.getName());
    response.setShiftStart(shift.getStartTime().toString());
    response.setShiftEnd(shift.getEndTime().toString());

    return response;
  }
}

package com.sgms.attendance;

import com.sgms.attendance.dto.AttendanceResponse;
import com.sgms.attendance.dto.CheckInRequest;
import com.sgms.attendance.dto.CheckOutRequest;
import com.sgms.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for attendance tracking operations
 * 
 * Manages guard check-in/out and attendance reporting.
 * 
 * Authorization:
 * - Check-in/out: ADMIN, SUPERVISOR, GUARD roles
 * - Reports: ADMIN, SUPERVISOR roles
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

  private final AttendanceService attendanceService;

  public AttendanceController(AttendanceService attendanceService) {
    this.attendanceService = attendanceService;
  }

  /**
   * Guard check-in
   * 
   * POST /api/attendance/check-in
   * Requires: ADMIN, SUPERVISOR, or GUARD role
   * 
   * Request body:
   * {
   *   "guardId": 1,
   *   "notes": "On time arrival"
   * }
   * 
   * Business Rules:
   * - Guard must have active assignment for today
   * - Cannot check in twice on same date
   * - Check-in window: 2h before to 2h after shift start
   * - Auto-calculates if LATE and late minutes
   */
  @PostMapping("/check-in")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'GUARD')")
  public ApiResponse<AttendanceResponse> checkIn(
      @Valid @RequestBody CheckInRequest request) {
    AttendanceResponse attendance = attendanceService.checkIn(request);
    return ApiResponse.success(attendance);
  }

  /**
   * Guard check-out
   * 
   * POST /api/attendance/check-out
   * Requires: ADMIN, SUPERVISOR, or GUARD role
   * 
   * Request body:
   * {
   *   "guardId": 1,
   *   "notes": "Shift completed"
   * }
   * 
   * Business Rules:
   * - Must have checked in today
   * - Cannot check out twice
   * - Auto-calculates if EARLY_LEAVE and early leave minutes
   * - Final status: PRESENT, LATE, or EARLY_LEAVE
   */
  @PostMapping("/check-out")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'GUARD')")
  public ApiResponse<AttendanceResponse> checkOut(
      @Valid @RequestBody CheckOutRequest request) {
    AttendanceResponse attendance = attendanceService.checkOut(request);
    return ApiResponse.success(attendance);
  }

  /**
   * Get attendance records for a specific guard
   * 
   * GET /api/attendance/guard/{guardId}
   * Requires: ADMIN or SUPERVISOR role
   * 
   * Returns all attendance history for the guard, newest first
   */
  @GetMapping("/guard/{guardId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<AttendanceResponse>> getGuardAttendance(
      @PathVariable Long guardId) {
    List<AttendanceResponse> attendance = attendanceService.getGuardAttendance(guardId);
    return ApiResponse.success(attendance);
  }

  /**
   * Get attendance records for a site on specific date
   * 
   * GET /api/attendance/site/{siteId}?date=2026-02-18
   * Requires: ADMIN or SUPERVISOR role
   * 
   * Query params:
   * - date (optional): YYYY-MM-DD format, defaults to today
   * 
   * Returns all attendance records for the site on the specified date
   */
  @GetMapping("/site/{siteId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<AttendanceResponse>> getSiteAttendance(
      @PathVariable Long siteId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    List<AttendanceResponse> attendance = attendanceService.getSiteAttendance(siteId, date);
    return ApiResponse.success(attendance);
  }

  /**
   * Get today's attendance summary
   * 
   * GET /api/attendance/today-summary
   * Requires: ADMIN or SUPERVISOR role
   * 
   * Returns all attendance records for current date across all sites
   * Useful for daily monitoring dashboard
   */
  @GetMapping("/today-summary")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<AttendanceResponse>> getTodaySummary() {
    List<AttendanceResponse> attendance = attendanceService.getTodaySummary();
    return ApiResponse.success(attendance);
  }

  /**
   * Get specific attendance record by ID
   * 
   * GET /api/attendance/{id}
   * Requires: ADMIN or SUPERVISOR role
   * 
   * Returns full details of a single attendance record
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<AttendanceResponse> getAttendanceById(
      @PathVariable Long id) {
    AttendanceResponse attendance = attendanceService.getAttendanceById(id);
    return ApiResponse.success(attendance);
  }
}

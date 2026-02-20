package com.sgms.attendance;

/**
 * AttendanceStatus Enum
 * 
 * Represents the status of a guard's daily attendance.
 * Status is auto-calculated based on check-in/out times vs. shift timings.
 * 
 * Status Determination Logic:
 * - PRESENT: Checked in on time (within 2h after shift start) and checked out
 * - LATE: Checked in after shift start time
 * - EARLY_LEAVE: Checked out before shift end time
 * - ABSENT: Never checked in (auto-marked by nightly scheduled job)
 * - MISSED_CHECKOUT: Checked in but never checked out (auto-marked by hourly job)
 */
public enum AttendanceStatus {
  
  /**
   * Guard checked in on time and completed shift
   */
  PRESENT,
  
  /**
   * Guard checked in after shift start time
   * Late duration is stored in lateMinutes field
   */
  LATE,
  
  /**
   * Guard checked out before shift end time
   * Early leave duration is stored in earlyLeaveMinutes field
   */
  EARLY_LEAVE,
  
  /**
   * Guard never checked in for assigned shift
   * Auto-marked by scheduled job at end of day
   */
  ABSENT,
  
  /**
   * Guard checked in but never checked out
   * Auto-marked by scheduled job (runs every hour after shift end + 2h buffer)
   */
  MISSED_CHECKOUT
}

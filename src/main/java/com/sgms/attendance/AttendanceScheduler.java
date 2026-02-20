package com.sgms.attendance;

import com.sgms.assignment.GuardAssignmentEntity;
import com.sgms.assignment.GuardAssignmentRepository;
import com.sgms.assignment.ShiftTypeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.Duration;
import java.util.List;

/**
 * AttendanceScheduler - Automated attendance status management
 * 
 * Runs scheduled jobs to automatically update attendance status:
 * 1. Mark absent guards (nightly job)
 * 2. Mark missed checkouts (hourly job)
 * 
 * Ensures data integrity and accurate attendance tracking without manual intervention.
 */
@Component
public class AttendanceScheduler {

  private static final Logger logger = LoggerFactory.getLogger(AttendanceScheduler.class);
  private static final int CHECKOUT_GRACE_HOURS = 2;

  private final AttendanceRepository attendanceRepository;
  private final GuardAssignmentRepository assignmentRepository;

  public AttendanceScheduler(
      AttendanceRepository attendanceRepository,
      GuardAssignmentRepository assignmentRepository) {
    this.attendanceRepository = attendanceRepository;
    this.assignmentRepository = assignmentRepository;
  }

  /**
   * Mark guards as ABSENT who never checked in
   * 
   * Runs every night at 11:59 PM
   * 
   * Logic:
   * 1. Find all guards with active assignments for today
   * 2. Check if attendance record exists
   * 3. If no attendance record, create one with ABSENT status
   * 
   * Cron: 0 59 23 * * * (every day at 23:59:00)
   */
  @Scheduled(cron = "0 59 23 * * *")
  @Transactional
  public void markAbsentGuards() {
    logger.info("Starting scheduled job: Mark absent guards");
    
    LocalDate today = LocalDate.now();
    int absentCount = 0;

    try {
      // Get all active assignments for today
      List<GuardAssignmentEntity> activeAssignments = assignmentRepository.findAllActiveAssignments()
          .stream()
          .filter(assignment -> {
            LocalDate effectiveFrom = assignment.getEffectiveFrom();
            LocalDate effectiveTo = assignment.getEffectiveTo();
            
            return !today.isBefore(effectiveFrom) && 
                   (effectiveTo == null || !today.isAfter(effectiveTo));
          })
          .toList();

      logger.info("Found {} active assignments for today", activeAssignments.size());

      // Check each assignment for attendance record
      for (GuardAssignmentEntity assignment : activeAssignments) {
        Long guardId = assignment.getGuard().getId();
        
        // Skip if attendance already recorded
        if (attendanceRepository.existsByGuardIdAndDate(guardId, today)) {
          continue;
        }

        // Create ABSENT record
        AttendanceEntity absentRecord = new AttendanceEntity();
        absentRecord.setGuard(assignment.getGuard());
        absentRecord.setAssignment(assignment);
        absentRecord.setAttendanceDate(today);
        absentRecord.setStatus(AttendanceStatus.ABSENT);
        absentRecord.setLateMinutes(0);
        absentRecord.setEarlyLeaveMinutes(0);
        absentRecord.setNotes("Auto-marked ABSENT by system (no check-in recorded)");
        absentRecord.setCheckInTime(null);
        absentRecord.setCheckOutTime(null);

        attendanceRepository.save(absentRecord);
        absentCount++;

        logger.debug("Marked guard {} as ABSENT for date {}", 
            guardId, today);
      }

      logger.info("Successfully marked {} guards as ABSENT", absentCount);

    } catch (Exception e) {
      logger.error("Error in markAbsentGuards scheduled job", e);
      throw e; // Re-throw to ensure transaction rollback
    }
  }

  /**
   * Mark guards as MISSED_CHECKOUT who checked in but never checked out
   * 
   * Runs every hour
   * 
   * Logic:
   * 1. Find all attendance records with check-in but no check-out
   * 2. Check if shift end time + grace period (2 hours) has passed
   * 3. If grace period passed, mark as MISSED_CHECKOUT
   * 
   * Cron: 0 0 * * * * (every hour at :00)
   */
  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void markMissedCheckouts() {
    logger.info("Starting scheduled job: Mark missed checkouts");
    
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    int missedCheckoutCount = 0;

    try {
      // Find all pending checkouts for today
      List<AttendanceEntity> pendingCheckouts = attendanceRepository.findPendingCheckouts(today);
      
      logger.info("Found {} pending checkouts", pendingCheckouts.size());

      for (AttendanceEntity attendance : pendingCheckouts) {
        ShiftTypeEntity shift = attendance.getAssignment().getShiftType();
        LocalTime shiftEnd = shift.getEndTime();
        
        // Calculate grace period deadline
        LocalDateTime shiftEndDateTime = LocalDateTime.of(today, shiftEnd);
        LocalDateTime gracePeriodDeadline = shiftEndDateTime.plusHours(CHECKOUT_GRACE_HOURS);
        
        // Handle overnight shifts (shift end is next day)
        boolean isOvernight = shift.getStartTime().isAfter(shift.getEndTime());
        if (isOvernight) {
          gracePeriodDeadline = gracePeriodDeadline.plusDays(1);
        }

        // Check if grace period has passed
        if (now.isAfter(gracePeriodDeadline)) {
          attendance.setStatus(AttendanceStatus.MISSED_CHECKOUT);
          
          String note = String.format(
              "Auto-marked MISSED_CHECKOUT by system (no checkout by %s + %d hour grace period)",
              shiftEnd, CHECKOUT_GRACE_HOURS
          );
          
          String existingNotes = attendance.getNotes();
          attendance.setNotes(existingNotes != null ? existingNotes + " | " + note : note);

          attendanceRepository.save(attendance);
          missedCheckoutCount++;

          logger.debug("Marked attendance {} as MISSED_CHECKOUT for guard {}", 
              attendance.getId(), attendance.getGuard().getId());
        }
      }

      logger.info("Successfully marked {} attendance records as MISSED_CHECKOUT", missedCheckoutCount);

    } catch (Exception e) {
      logger.error("Error in markMissedCheckouts scheduled job", e);
      throw e; // Re-throw to ensure transaction rollback
    }
  }

  /**
   * Daily cleanup and validation job
   * 
   * Runs every day at 1:00 AM
   * 
   * Purpose:
   * - Log attendance statistics
   * - Validate data integrity
   * - Optional: Archive old records
   * 
   * Cron: 0 0 1 * * * (every day at 01:00:00)
   */
  @Scheduled(cron = "0 0 1 * * *")
  @Transactional(readOnly = true)
  public void dailyAttendanceReport() {
    logger.info("Starting scheduled job: Daily attendance report");
    
    LocalDate yesterday = LocalDate.now().minusDays(1);

    try {
      // Get attendance summary
      List<Object[]> summary = attendanceRepository.getAttendanceSummaryByDate(yesterday);
      
      logger.info("=== ATTENDANCE SUMMARY FOR {} ===", yesterday);
      
      int total = 0;
      for (Object[] row : summary) {
        AttendanceStatus status = (AttendanceStatus) row[0];
        Long count = (Long) row[1];
        total += count;
        logger.info("{}: {}", status, count);
      }
      
      logger.info("TOTAL: {}", total);
      logger.info("================================");

    } catch (Exception e) {
      logger.error("Error in dailyAttendanceReport scheduled job", e);
    }
  }
}

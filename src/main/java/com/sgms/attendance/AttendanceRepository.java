package com.sgms.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Attendance entity
 * 
 * Provides data access methods for attendance tracking including:
 * - Check-in/out operations
 * - Guard attendance history
 * - Site attendance reports
 * - Scheduled job queries (absent guards, missed checkouts)
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

  /**
   * Find attendance record for a guard on specific date
   * Used to prevent duplicate check-ins and for checkout operations
   */
  @Query("SELECT a FROM AttendanceEntity a WHERE a.guard.id = :guardId AND a.attendanceDate = :date")
  Optional<AttendanceEntity> findByGuardIdAndDate(Long guardId, LocalDate date);

  /**
   * Find all attendance records for a guard
   * Ordered by date descending (most recent first)
   */
  @Query("SELECT a FROM AttendanceEntity a WHERE a.guard.id = :guardId ORDER BY a.attendanceDate DESC")
  List<AttendanceEntity> findByGuardId(Long guardId);

  /**
   * Find attendance records for a guard within date range
   * Used for attendance reports and history
   */
  @Query("SELECT a FROM AttendanceEntity a " +
         "WHERE a.guard.id = :guardId " +
         "AND a.attendanceDate >= :startDate " +
         "AND a.attendanceDate <= :endDate " +
         "ORDER BY a.attendanceDate DESC")
  List<AttendanceEntity> findByGuardIdAndDateRange(Long guardId, LocalDate startDate, LocalDate endDate);

  /**
   * Find all attendance records for a specific site on a date
   * Used for site-level attendance reports
   */
  @Query("SELECT a FROM AttendanceEntity a " +
         "WHERE a.assignment.sitePost.site.id = :siteId " +
         "AND a.attendanceDate = :date " +
         "ORDER BY a.checkInTime")
  List<AttendanceEntity> findBySiteIdAndDate(Long siteId, LocalDate date);

  /**
   * Find all attendance records for today
   * Used for today's summary dashboard
   */
  @Query("SELECT a FROM AttendanceEntity a " +
         "WHERE a.attendanceDate = :date " +
         "ORDER BY a.checkInTime")
  List<AttendanceEntity> findByDate(LocalDate date);

  /**
   * Find guards who checked in but haven't checked out yet
   * Used by scheduled job to mark MISSED_CHECKOUT status
   */
  @Query("SELECT a FROM AttendanceEntity a " +
         "WHERE a.attendanceDate = :date " +
         "AND a.checkInTime IS NOT NULL " +
         "AND a.checkOutTime IS NULL " +
         "AND a.status != 'MISSED_CHECKOUT'")
  List<AttendanceEntity> findPendingCheckouts(LocalDate date);

  /**
   * Check if guard already has attendance record for the date
   * Used to enforce one-record-per-guard-per-date rule
   */
  @Query("SELECT COUNT(a) > 0 FROM AttendanceEntity a " +
         "WHERE a.guard.id = :guardId " +
         "AND a.attendanceDate = :date")
  boolean existsByGuardIdAndDate(Long guardId, LocalDate date);

  /**
   * Find attendance records by status
   * Used for filtering and reporting
   */
  @Query("SELECT a FROM AttendanceEntity a " +
         "WHERE a.status = :status " +
         "AND a.attendanceDate = :date " +
         "ORDER BY a.checkInTime")
  List<AttendanceEntity> findByStatusAndDate(AttendanceStatus status, LocalDate date);

  /**
   * Get attendance summary counts for a date
   * Returns count of each status type
   */
  @Query("SELECT a.status, COUNT(a) FROM AttendanceEntity a " +
         "WHERE a.attendanceDate = :date " +
         "GROUP BY a.status")
  List<Object[]> getAttendanceSummaryByDate(LocalDate date);

  /**
   * Find all attendance records for a site post on specific date
   * Used for post-level attendance tracking
   */
  @Query("SELECT a FROM AttendanceEntity a " +
         "WHERE a.assignment.sitePost.id = :sitePostId " +
         "AND a.attendanceDate = :date " +
         "ORDER BY a.checkInTime")
  List<AttendanceEntity> findBySitePostIdAndDate(Long sitePostId, LocalDate date);
}

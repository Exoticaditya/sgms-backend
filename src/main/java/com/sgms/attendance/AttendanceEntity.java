package com.sgms.attendance;

import com.sgms.assignment.GuardAssignmentEntity;
import com.sgms.guard.GuardEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/**
 * AttendanceEntity - Core attendance tracking entity
 * 
 * Represents a guard's daily attendance record with check-in/out times.
 * Status is auto-calculated based on shift timings and actual check-in/out
 * times.
 * 
 * Table: attendance_logs
 * 
 * Business Rules:
 * - One attendance record per guard per date (enforced by UK constraint)
 * - Guard must have active assignment for the date
 * - Check-in window: 2 hours before to 2 hours after shift start
 * - Auto-mark LATE if checked in after shift start
 * - Auto-mark EARLY_LEAVE if checked out before shift end
 * - Auto-mark MISSED_CHECKOUT if no checkout by end of shift + 2 hours
 * - Auto-mark ABSENT if no check-in by end of day
 */
@Entity
@Table(name = "attendance_logs")
public class AttendanceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "guard_id")
  private GuardEntity guard;

  @ManyToOne
  @JoinColumn(name = "assignment_id")
  private GuardAssignmentEntity assignment;

  @Column(name = "attendance_date", nullable = false)
  private LocalDate attendanceDate;

  @Column(name = "check_in_time")
  private Instant checkInTime;

  @Column(name = "check_out_time")
  private Instant checkOutTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AttendanceStatus status;

  @Column(name = "late_minutes", nullable = false)
  private Integer lateMinutes = 0;

  @Column(name = "early_leave_minutes", nullable = false)
  private Integer earlyLeaveMinutes = 0;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
    if (status == null) {
      status = AttendanceStatus.PRESENT;
    }
    if (lateMinutes == null) {
      lateMinutes = 0;
    }
    if (earlyLeaveMinutes == null) {
      earlyLeaveMinutes = 0;
    }
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Instant.now();
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public GuardEntity getGuard() {
    return guard;
  }

  public void setGuard(GuardEntity guard) {
    this.guard = guard;
  }

  public GuardAssignmentEntity getAssignment() {
    return assignment;
  }

  public void setAssignment(GuardAssignmentEntity assignment) {
    this.assignment = assignment;
  }

  public LocalDate getAttendanceDate() {
    return attendanceDate;
  }

  public void setAttendanceDate(LocalDate attendanceDate) {
    this.attendanceDate = attendanceDate;
  }

  public Instant getCheckInTime() {
    return checkInTime;
  }

  public void setCheckInTime(Instant checkInTime) {
    this.checkInTime = checkInTime;
  }

  public Instant getCheckOutTime() {
    return checkOutTime;
  }

  public void setCheckOutTime(Instant checkOutTime) {
    this.checkOutTime = checkOutTime;
  }

  public AttendanceStatus getStatus() {
    return status;
  }

  public void setStatus(AttendanceStatus status) {
    this.status = status;
  }

  public Integer getLateMinutes() {
    return lateMinutes;
  }

  public void setLateMinutes(Integer lateMinutes) {
    this.lateMinutes = lateMinutes;
  }

  public Integer getEarlyLeaveMinutes() {
    return earlyLeaveMinutes;
  }

  public void setEarlyLeaveMinutes(Integer earlyLeaveMinutes) {
    this.earlyLeaveMinutes = earlyLeaveMinutes;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}

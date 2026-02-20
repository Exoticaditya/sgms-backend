package com.sgms.attendance.dto;

import com.sgms.attendance.AttendanceStatus;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for attendance records
 * 
 * Denormalized structure containing all attendance details
 * including guard, site, client, shift, and timing information.
 * 
 * Used for:
 * - Individual attendance record retrieval
 * - Guard attendance history
 * - Site attendance reports
 * - Today's attendance summary
 */
public class AttendanceResponse {

  // Attendance record fields
  private Long attendanceId;
  private LocalDate attendanceDate;
  private Instant checkInTime;
  private Instant checkOutTime;
  private AttendanceStatus status;
  private Integer lateMinutes;
  private Integer earlyLeaveMinutes;
  private String notes;

  // Guard details
  private Long guardId;
  private String guardFirstName;
  private String guardLastName;
  private String guardFullName;
  private String employeeCode;

  // Assignment details
  private Long assignmentId;

  // Site post details
  private Long sitePostId;
  private String postName;

  // Site details
  private Long siteId;
  private String siteName;

  // Client details
  private Long clientId;
  private String clientName;

  // Shift details
  private String shiftName;
  private String shiftStart;  // HH:mm format
  private String shiftEnd;    // HH:mm format

  // Audit
  private Instant createdAt;
  private Instant updatedAt;

  // Getters and Setters

  public Long getAttendanceId() {
    return attendanceId;
  }

  public void setAttendanceId(Long attendanceId) {
    this.attendanceId = attendanceId;
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

  public Long getGuardId() {
    return guardId;
  }

  public void setGuardId(Long guardId) {
    this.guardId = guardId;
  }

  public String getGuardFirstName() {
    return guardFirstName;
  }

  public void setGuardFirstName(String guardFirstName) {
    this.guardFirstName = guardFirstName;
  }

  public String getGuardLastName() {
    return guardLastName;
  }

  public void setGuardLastName(String guardLastName) {
    this.guardLastName = guardLastName;
  }

  public String getGuardFullName() {
    return guardFullName;
  }

  public void setGuardFullName(String guardFullName) {
    this.guardFullName = guardFullName;
  }

  public String getEmployeeCode() {
    return employeeCode;
  }

  public void setEmployeeCode(String employeeCode) {
    this.employeeCode = employeeCode;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
  }

  public Long getSitePostId() {
    return sitePostId;
  }

  public void setSitePostId(Long sitePostId) {
    this.sitePostId = sitePostId;
  }

  public String getPostName() {
    return postName;
  }

  public void setPostName(String postName) {
    this.postName = postName;
  }

  public Long getSiteId() {
    return siteId;
  }

  public void setSiteId(Long siteId) {
    this.siteId = siteId;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public Long getClientId() {
    return clientId;
  }

  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getShiftName() {
    return shiftName;
  }

  public void setShiftName(String shiftName) {
    this.shiftName = shiftName;
  }

  public String getShiftStart() {
    return shiftStart;
  }

  public void setShiftStart(String shiftStart) {
    this.shiftStart = shiftStart;
  }

  public String getShiftEnd() {
    return shiftEnd;
  }

  public void setShiftEnd(String shiftEnd) {
    this.shiftEnd = shiftEnd;
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

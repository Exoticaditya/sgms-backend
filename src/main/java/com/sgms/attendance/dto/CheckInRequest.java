package com.sgms.attendance.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for guard check-in
 * 
 * Guard provides their ID to check in for their assigned shift.
 * System automatically:
 * - Validates guard has active assignment for today
 * - Determines shift timing from assignment
 * - Calculates if guard is late
 * - Creates attendance record with status
 */
public class CheckInRequest {

  @NotNull(message = "Guard ID is required")
  private Long guardId;

  /**
   * Optional notes from guard at check-in
   * Examples: "Traffic delay", "Site access issue", etc.
   */
  private String notes;

  // Getters and Setters

  public Long getGuardId() {
    return guardId;
  }

  public void setGuardId(Long guardId) {
    this.guardId = guardId;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}

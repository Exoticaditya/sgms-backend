package com.sgms.attendance.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for guard check-out
 * 
 * Guard provides their ID to check out from their shift.
 * System automatically:
 * - Finds today's attendance record for the guard
 * - Validates guard has checked in
 * - Determines if guard left early
 * - Updates attendance record with checkout time and final status
 */
public class CheckOutRequest {

  @NotNull(message = "Guard ID is required")
  private Long guardId;

  /**
   * Optional notes from guard at check-out
   * Examples: "Shift completed", "Emergency leave", etc.
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

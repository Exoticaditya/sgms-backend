package com.sgms.assignment.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO for creating a guard assignment
 */
public class CreateAssignmentRequest {

  @NotNull(message = "Guard ID is required")
  private Long guardId;

  @NotNull(message = "Site post ID is required")
  private Long sitePostId;

  @NotNull(message = "Shift type ID is required")
  private Long shiftTypeId;

  @NotNull(message = "Effective from date is required")
  private LocalDate effectiveFrom;

  private LocalDate effectiveTo;

  private String notes;

  // Getters and Setters

  public Long getGuardId() {
    return guardId;
  }

  public void setGuardId(Long guardId) {
    this.guardId = guardId;
  }

  public Long getSitePostId() {
    return sitePostId;
  }

  public void setSitePostId(Long sitePostId) {
    this.sitePostId = sitePostId;
  }

  public Long getShiftTypeId() {
    return shiftTypeId;
  }

  public void setShiftTypeId(Long shiftTypeId) {
    this.shiftTypeId = shiftTypeId;
  }

  public LocalDate getEffectiveFrom() {
    return effectiveFrom;
  }

  public void setEffectiveFrom(LocalDate effectiveFrom) {
    this.effectiveFrom = effectiveFrom;
  }

  public LocalDate getEffectiveTo() {
    return effectiveTo;
  }

  public void setEffectiveTo(LocalDate effectiveTo) {
    this.effectiveTo = effectiveTo;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}

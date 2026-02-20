package com.sgms.assignment.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for guard assignment information
 * 
 * Contains denormalized data for efficient client consumption
 */
public class AssignmentResponse {

  private Long id;
  private Long guardId;
  private String guardEmployeeCode;
  private String guardName;
  private Long sitePostId;
  private String sitePostName;
  private Long siteId;
  private String siteName;
  private Long clientId;
  private String clientName;
  private Long shiftTypeId;
  private String shiftTypeName;
  private LocalTime shiftStartTime;
  private LocalTime shiftEndTime;
  private LocalDate effectiveFrom;
  private LocalDate effectiveTo;
  private String status;
  private String notes;
  private Instant createdAt;
  private Instant updatedAt;
  private Long createdByUserId;
  private String createdByEmail;

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getGuardId() {
    return guardId;
  }

  public void setGuardId(Long guardId) {
    this.guardId = guardId;
  }

  public String getGuardEmployeeCode() {
    return guardEmployeeCode;
  }

  public void setGuardEmployeeCode(String guardEmployeeCode) {
    this.guardEmployeeCode = guardEmployeeCode;
  }

  public String getGuardName() {
    return guardName;
  }

  public void setGuardName(String guardName) {
    this.guardName = guardName;
  }

  public Long getSitePostId() {
    return sitePostId;
  }

  public void setSitePostId(Long sitePostId) {
    this.sitePostId = sitePostId;
  }

  public String getSitePostName() {
    return sitePostName;
  }

  public void setSitePostName(String sitePostName) {
    this.sitePostName = sitePostName;
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

  public Long getShiftTypeId() {
    return shiftTypeId;
  }

  public void setShiftTypeId(Long shiftTypeId) {
    this.shiftTypeId = shiftTypeId;
  }

  public String getShiftTypeName() {
    return shiftTypeName;
  }

  public void setShiftTypeName(String shiftTypeName) {
    this.shiftTypeName = shiftTypeName;
  }

  public LocalTime getShiftStartTime() {
    return shiftStartTime;
  }

  public void setShiftStartTime(LocalTime shiftStartTime) {
    this.shiftStartTime = shiftStartTime;
  }

  public LocalTime getShiftEndTime() {
    return shiftEndTime;
  }

  public void setShiftEndTime(LocalTime shiftEndTime) {
    this.shiftEndTime = shiftEndTime;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public Long getCreatedByUserId() {
    return createdByUserId;
  }

  public void setCreatedByUserId(Long createdByUserId) {
    this.createdByUserId = createdByUserId;
  }

  public String getCreatedByEmail() {
    return createdByEmail;
  }

  public void setCreatedByEmail(String createdByEmail) {
    this.createdByEmail = createdByEmail;
  }
}

package com.sgms.site.dto;

import java.time.Instant;

/**
 * Response DTO for supervisor site assignment information
 */
public class SupervisorSiteResponse {

  private Long id;
  private Long supervisorUserId;
  private String supervisorName;
  private String supervisorEmail;
  private Long siteId;
  private String siteName;
  private Instant assignedAt;
  private Instant removedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSupervisorUserId() {
    return supervisorUserId;
  }

  public void setSupervisorUserId(Long supervisorUserId) {
    this.supervisorUserId = supervisorUserId;
  }

  public String getSupervisorName() {
    return supervisorName;
  }

  public void setSupervisorName(String supervisorName) {
    this.supervisorName = supervisorName;
  }

  public String getSupervisorEmail() {
    return supervisorEmail;
  }

  public void setSupervisorEmail(String supervisorEmail) {
    this.supervisorEmail = supervisorEmail;
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

  public Instant getAssignedAt() {
    return assignedAt;
  }

  public void setAssignedAt(Instant assignedAt) {
    this.assignedAt = assignedAt;
  }

  public Instant getRemovedAt() {
    return removedAt;
  }

  public void setRemovedAt(Instant removedAt) {
    this.removedAt = removedAt;
  }
}

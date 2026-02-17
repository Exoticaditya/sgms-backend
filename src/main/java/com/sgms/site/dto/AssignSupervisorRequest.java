package com.sgms.site.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for assigning a supervisor to a site
 */
public class AssignSupervisorRequest {

  @NotNull(message = "Supervisor user ID is required")
  private Long supervisorUserId;

  @NotNull(message = "Site ID is required")
  private Long siteId;

  public Long getSupervisorUserId() {
    return supervisorUserId;
  }

  public void setSupervisorUserId(Long supervisorUserId) {
    this.supervisorUserId = supervisorUserId;
  }

  public Long getSiteId() {
    return siteId;
  }

  public void setSiteId(Long siteId) {
    this.siteId = siteId;
  }
}

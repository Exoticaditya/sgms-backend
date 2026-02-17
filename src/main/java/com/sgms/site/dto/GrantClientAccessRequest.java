package com.sgms.site.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for granting a client user access to a site
 */
public class GrantClientAccessRequest {

  @NotNull(message = "Client user ID is required")
  private Long clientUserId;

  @NotNull(message = "Site ID is required")
  private Long siteId;

  public Long getClientUserId() {
    return clientUserId;
  }

  public void setClientUserId(Long clientUserId) {
    this.clientUserId = clientUserId;
  }

  public Long getSiteId() {
    return siteId;
  }

  public void setSiteId(Long siteId) {
    this.siteId = siteId;
  }
}

package com.sgms.site.dto;

import java.time.Instant;

/**
 * Response DTO for client site access information
 */
public class ClientSiteAccessResponse {

  private Long id;
  private Long clientUserId;
  private String clientName;
  private String clientEmail;
  private Long siteId;
  private String siteName;
  private Instant grantedAt;
  private Instant revokedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getClientUserId() {
    return clientUserId;
  }

  public void setClientUserId(Long clientUserId) {
    this.clientUserId = clientUserId;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getClientEmail() {
    return clientEmail;
  }

  public void setClientEmail(String clientEmail) {
    this.clientEmail = clientEmail;
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

  public Instant getGrantedAt() {
    return grantedAt;
  }

  public void setGrantedAt(Instant grantedAt) {
    this.grantedAt = grantedAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }
}

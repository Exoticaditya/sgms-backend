package com.sgms.site.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for site information
 */
public class SiteResponse {

  private Long id;
  private Long clientAccountId;
  private String clientAccountName;
  private String name;
  private String address;
  private BigDecimal latitude;
  private BigDecimal longitude;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant deletedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getClientAccountId() {
    return clientAccountId;
  }

  public void setClientAccountId(Long clientAccountId) {
    this.clientAccountId = clientAccountId;
  }

  public String getClientAccountName() {
    return clientAccountName;
  }

  public void setClientAccountName(String clientAccountName) {
    this.clientAccountName = clientAccountName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }
}

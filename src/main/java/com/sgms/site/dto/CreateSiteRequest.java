package com.sgms.site.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new site
 */
public class CreateSiteRequest {

  @NotNull(message = "Client account ID is required")
  private Long clientAccountId;

  @NotBlank(message = "Site name is required")
  @Size(max = 255, message = "Site name must not exceed 255 characters")
  private String name;

  @Size(max = 500, message = "Address must not exceed 500 characters")
  private String address;

  @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
  private BigDecimal latitude;

  @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
  private BigDecimal longitude;

  public Long getClientAccountId() {
    return clientAccountId;
  }

  public void setClientAccountId(Long clientAccountId) {
    this.clientAccountId = clientAccountId;
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
}

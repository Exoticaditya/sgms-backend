package com.sgms.site.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new site post
 */
public class CreateSitePostRequest {

  @NotNull(message = "Site ID is required")
  private Long siteId;

  @NotBlank(message = "Post name is required")
  @Size(max = 255, message = "Post name must not exceed 255 characters")
  private String postName;

  @Size(max = 5000, message = "Description must not exceed 5000 characters")
  private String description;

  @NotNull(message = "Required guards is required")
  @Min(value = 1, message = "Required guards must be at least 1")
  private Integer requiredGuards;

  public Long getSiteId() {
    return siteId;
  }

  public void setSiteId(Long siteId) {
    this.siteId = siteId;
  }

  public String getPostName() {
    return postName;
  }

  public void setPostName(String postName) {
    this.postName = postName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getRequiredGuards() {
    return requiredGuards;
  }

  public void setRequiredGuards(Integer requiredGuards) {
    this.requiredGuards = requiredGuards;
  }
}

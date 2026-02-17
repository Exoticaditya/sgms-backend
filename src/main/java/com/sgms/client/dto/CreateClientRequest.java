package com.sgms.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new client account
 */
public class CreateClientRequest {

  @NotBlank(message = "Client name is required")
  @Size(max = 255, message = "Client name must not exceed 255 characters")
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}

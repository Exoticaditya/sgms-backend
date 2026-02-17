package com.sgms.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard API Response Wrapper
 * 
 * All API endpoints must return this wrapper for consistency.
 * Provides uniform response structure across the entire platform.
 * 
 * @param <T> The type of data being returned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  
  private boolean success;
  private T data;
  private String message;
  private Instant timestamp;

  public ApiResponse() {
    this.timestamp = Instant.now();
  }

  public ApiResponse(boolean success, T data, String message) {
    this.success = success;
    this.data = data;
    this.message = message;
    this.timestamp = Instant.now();
  }

  // Factory methods for common response types
  
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, null);
  }

  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, data, message);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, null, message);
  }

  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(true, data, "Resource created successfully");
  }

  public static <T> ApiResponse<T> created(T data, String message) {
    return new ApiResponse<>(true, data, message);
  }

  // Getters and Setters

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}

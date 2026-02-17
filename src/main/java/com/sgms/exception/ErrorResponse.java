package com.sgms.exception;

import java.time.Instant;

/**
 * Standard Error Response
 * 
 * Returned by GlobalExceptionHandler for all error scenarios.
 * Provides consistent error structure across the platform.
 * Never exposes stacktraces to clients.
 */
public class ErrorResponse {
  
  private String error;
  private String message;
  private int status;
  private Instant timestamp;
  private String path;

  public ErrorResponse() {
    this.timestamp = Instant.now();
  }

  public ErrorResponse(String error, String message, int status, String path) {
    this.error = error;
    this.message = message;
    this.status = status;
    this.path = path;
    this.timestamp = Instant.now();
  }

  // Getters and Setters

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}

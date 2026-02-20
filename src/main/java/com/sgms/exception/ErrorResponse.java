package com.sgms.exception;

import java.time.Clock;
import java.time.Instant;

/**
 * Standard Error Response
 * 
 * Returned by GlobalExceptionHandler for all error scenarios.
 * Matches ApiResponse wrapper format with success=false.
 * Never exposes stacktraces to clients.
 */
public class ErrorResponse {
  
  private final boolean success = false;
  private String message;
  private Instant timestamp;
  private String path;
  private transient Clock clock;

  public ErrorResponse() {
    this.clock = Clock.systemUTC();
    this.timestamp = Instant.now(clock);
  }

  public ErrorResponse(String message, String path) {
    this.clock = Clock.systemUTC();
    this.message = message;
    this.path = path;
    this.timestamp = Instant.now(clock);
  }

  public ErrorResponse(String message, String path, Clock clock) {
    this.clock = clock;
    this.message = message;
    this.path = path;
    this.timestamp = clock.instant();
  }

  // Getters and Setters

  public boolean isSuccess() {
    return success;
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

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}

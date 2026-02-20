package com.sgms.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 * 
 * Centralized exception handling for all REST controllers.
 * Returns standardized ErrorResponse for all errors.
 * Never exposes stacktraces to clients (logged server-side only).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final Clock clock;

  public GlobalExceptionHandler(Clock clock) {
    this.clock = clock;
  }

  /**
   * Handle validation errors (Bean Validation)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    String message = "Validation failed: " + errors.entrySet().stream()
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining(", "));

    ErrorResponse errorResponse = new ErrorResponse(message, request.getRequestURI(), clock);

    logger.warn("Validation error: {} on {}", message, request.getRequestURI());
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle illegal argument exceptions
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex,
      HttpServletRequest request) {
    
    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI(), clock);

    logger.warn("Invalid argument: {} on {}", ex.getMessage(), request.getRequestURI());
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle access denied exceptions
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex,
      HttpServletRequest request) {
    
    ErrorResponse errorResponse = new ErrorResponse(
        "You do not have permission to access this resource",
        request.getRequestURI(),
        clock
    );

    logger.warn("Access denied: {} attempted to access {}", 
        request.getRemoteUser(), request.getRequestURI());
    
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  /**
   * Handle username not found exceptions
   */
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
      UsernameNotFoundException ex,
      HttpServletRequest request) {
    
    ErrorResponse errorResponse = new ErrorResponse("Invalid credentials", request.getRequestURI(), clock);

    logger.warn("Username not found: {} on {}", ex.getMessage(), request.getRequestURI());
    
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * Handle ResponseStatusException
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      ResponseStatusException ex,
      HttpServletRequest request) {
    
    String message = ex.getReason() != null ? ex.getReason() : "An error occurred";
    ErrorResponse errorResponse = new ErrorResponse(message, request.getRequestURI(), clock);

    logger.warn("Response status exception: {} on {}", ex.getReason(), request.getRequestURI());
    
    return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
  }

  /**
   * Handle all other exceptions (fallback)
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex,
      HttpServletRequest request) {
    
    ErrorResponse errorResponse = new ErrorResponse(
        "An unexpected error occurred. Please try again later.",
        request.getRequestURI(),
        clock
    );

    // Log full stacktrace server-side (never sent to client)
    logger.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}

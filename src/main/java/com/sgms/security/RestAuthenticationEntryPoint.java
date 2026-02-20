package com.sgms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgms.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;

/**
 * REST Authentication Entry Point
 * 
 * Handles authentication failures for API endpoints.
 * Returns JSON 401 response instead of redirecting to login page.
 * 
 * This ensures API-only behavior (no HTML responses).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Clock clock;

  public RestAuthenticationEntryPoint(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {

    // Create standardized error response
    ErrorResponse errorResponse = new ErrorResponse(
        "Authentication required",
        request.getRequestURI(),
        clock
    );

    // Set response properties
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    
    // Write JSON response
    objectMapper.writeValue(response.getOutputStream(), errorResponse);
  }
}

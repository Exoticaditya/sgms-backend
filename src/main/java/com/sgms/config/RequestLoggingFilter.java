package com.sgms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Request Logging Filter
 * 
 * Logs all API requests with:
 * - HTTP method
 * - Endpoint path
 * - Response status
 * - Processing time (ms)
 * - Authenticated username (if exists)
 * 
 * Format: [API] {status} {method} {path} {time}ms user={username}
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger("API_REQUEST");

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    long startTime = System.currentTimeMillis();
    String method = request.getMethod();
    String path = request.getRequestURI();

    try {
      // Continue the filter chain
      filterChain.doFilter(request, response);
    } finally {
      // Calculate processing time
      long processingTime = System.currentTimeMillis() - startTime;
      int status = response.getStatus();
      
      // Extract authenticated username if available
      String username = getAuthenticatedUsername();
      
      // Format and log
      if (username != null) {
        logger.info("[API] {} {} {} {}ms user={}", 
            status, method, path, processingTime, username);
      } else {
        logger.info("[API] {} {} {} {}ms", 
            status, method, path, processingTime);
      }
    }
  }

  /**
   * Extract authenticated username from SecurityContext
   * 
   * @return username or null if not authenticated
   */
  private String getAuthenticatedUsername() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated() 
          && !"anonymousUser".equals(authentication.getPrincipal())) {
        return authentication.getName();
      }
    } catch (Exception e) {
      // Ignore - return null for unauthenticated requests
    }
    return null;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    // Don't log actuator health checks (too noisy)
    String path = request.getRequestURI();
    return path.startsWith("/actuator/health");
  }
}

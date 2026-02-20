package com.sgms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgms.exception.ErrorResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService, ObjectMapper objectMapper, Clock clock) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring("Bearer ".length()).trim();
    try {
      Jws<Claims> parsed = jwtService.parseAndValidate(token);
      String email = parsed.getPayload().get("email", String.class);
      if (email == null || email.isBlank()) {
        unauthorized(response, "Invalid token");
        return;
      }

      UserDetails userDetails = userDetailsService.loadUserByUsername(email);
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (Exception ex) {
      unauthorized(response, "Unauthorized");
      return; // CRITICAL: Stop filter chain after sending error response
    }
  }

  private void unauthorized(HttpServletResponse response, String message) throws IOException {
    ErrorResponse errorResponse = new ErrorResponse(message, "/api", clock);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), errorResponse);
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    
    // Skip JWT filter only for public authentication endpoints (login and register)
    // /api/auth/me requires authentication, so don't skip it
    return path.equals("/api/auth/login") ||
           path.equals("/api/auth/register") ||
           path.startsWith("/actuator/health");
  }
}

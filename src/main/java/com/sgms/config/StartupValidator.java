package com.sgms.config;

import com.sgms.security.JwtProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Startup Configuration Validator
 * 
 * Validates critical configuration at application startup.
 * Fails fast if required configuration is missing or invalid.
 * 
 * This prevents the application from starting in an invalid state
 * and provides clear error messages for operators.
 */
@Configuration
public class StartupValidator {
  
  private static final Logger logger = LoggerFactory.getLogger(StartupValidator.class);
  
  private final JwtProperties jwtProperties;
  private final DataSource dataSource;
  
  @Value("${app.cors.allowed-origins:}")
  private String allowedOrigins;
  
  public StartupValidator(JwtProperties jwtProperties, DataSource dataSource) {
    this.jwtProperties = jwtProperties;
    this.dataSource = dataSource;
  }
  
  @PostConstruct
  public void validateConfiguration() {
    logger.info("=== Starting Configuration Validation ===");
    
    validateJwtConfiguration();
    validateDatabaseConnection();
    validateCorsConfiguration();
    
    logger.info("=== Configuration Validation Complete - All Checks Passed ===");
  }
  
  private void validateJwtConfiguration() {
    logger.info("Validating JWT configuration...");
    
    String secret = jwtProperties.getSecret();
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException(
          "FATAL: JWT secret is not configured. Set APP_SECURITY_JWT_SECRET environment variable."
      );
    }
    
    if (secret.length() < 32) {
      throw new IllegalStateException(
          "FATAL: JWT secret must be at least 32 characters. Current length: " + secret.length()
      );
    }
    
    if (jwtProperties.getAccessTokenTtlSeconds() <= 0) {
      throw new IllegalStateException(
          "FATAL: JWT token TTL must be positive. Current value: " + 
          jwtProperties.getAccessTokenTtlSeconds()
      );
    }
    
    logger.info("✓ JWT configuration valid (secret length: {}, TTL: {}s, issuer: {})",
        secret.length(), jwtProperties.getAccessTokenTtlSeconds(), jwtProperties.getIssuer());
  }
  
  private void validateDatabaseConnection() {
    logger.info("Validating database connection...");
    
    try (Connection connection = dataSource.getConnection()) {
      if (!connection.isValid(5)) {
        throw new IllegalStateException("FATAL: Database connection is not valid");
      }
      
      String dbProductName = connection.getMetaData().getDatabaseProductName();
      String dbProductVersion = connection.getMetaData().getDatabaseProductVersion();
      
      logger.info("✓ Database connection valid ({} {})", dbProductName, dbProductVersion);
    } catch (Exception e) {
      throw new IllegalStateException(
          "FATAL: Cannot connect to database. Check DATABASE_URL or datasource configuration.",
          e
      );
    }
  }
  
  private void validateCorsConfiguration() {
    logger.info("Validating CORS configuration...");
    
    if (allowedOrigins == null || allowedOrigins.isBlank()) {
      logger.warn("⚠ CORS allowed origins not configured - using production defaults");
    } else {
      String[] origins = allowedOrigins.split(",");
      logger.info("✓ CORS configured with {} origin(s)", origins.length);
      for (String origin : origins) {
        logger.info("  - {}", origin.trim());
      }
    }
  }
}

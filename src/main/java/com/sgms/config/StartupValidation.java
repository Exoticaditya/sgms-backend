package com.sgms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Production Startup Validation
 * 
 * Validates critical requirements before allowing application to start:
 * 1. Database connection is successful
 * 2. Required tables exist in database
 * 3. JWT secret is properly configured
 * 
 * If any validation fails, application will EXIT to prevent running in unsafe state.
 */
@Component
@Profile("prod")
public class StartupValidation implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger logger = LoggerFactory.getLogger(StartupValidation.class);

  private final DataSource dataSource;
  private final JdbcTemplate jdbcTemplate;

  // Required database tables for SGMS
  // PHASE 1: Core authentication and guards
  // PHASE 2: Client and site management
  // PHASE 3: Site posts and access control
  private static final List<String> REQUIRED_TABLES = List.of(
      "users",
      "guards",
      "client_accounts",
      "sites",
      "site_posts",
      "supervisor_site_mapping",
      "client_site_access"
  );

  public StartupValidation(DataSource dataSource, JdbcTemplate jdbcTemplate) {
    this.dataSource = dataSource;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
    logger.info("=== SGMS Production Startup Validation ===");
    
    try {
      validateDatabaseConnection();
      validateRequiredTables();
      validateJwtSecret();
      
      logger.info("✓ All startup validations passed");
      logger.info("✓ SGMS Backend is ready for production traffic");
      logger.info("==========================================");
      
    } catch (Exception e) {
      logger.error("✗ FATAL: Startup validation failed", e);
      logger.error("✗ Application cannot start in unsafe state");
      logger.error("✗ Exiting application...");
      System.exit(1);
    }
  }

  /**
   * Validate database connection is alive and reachable
   */
  private void validateDatabaseConnection() {
    logger.info("Validating database connection...");
    try (Connection connection = dataSource.getConnection()) {
      if (connection == null || !connection.isValid(5)) {
        throw new IllegalStateException("Database connection is not valid");
      }
      logger.info("✓ Database connection successful");
      logger.info("  Database: {}", connection.getMetaData().getURL());
    } catch (SQLException e) {
      throw new IllegalStateException("Failed to establish database connection", e);
    }
  }

  /**
   * Validate all required database tables exist
   * 
   * This ensures schema migrations have been applied before application starts
   */
  private void validateRequiredTables() {
    logger.info("Validating required database tables...");
    
    for (String tableName : REQUIRED_TABLES) {
      String query = """
          SELECT EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name = ?
          )
          """;
      
      Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, tableName);
      
      if (exists == null || !exists) {
        throw new IllegalStateException(
            String.format("Required table '%s' does not exist in database. " +
                "Please run schema migrations before starting application.", tableName)
        );
      }
      
      logger.info("  ✓ Table '{}' exists", tableName);
    }
    
    logger.info("✓ All required tables exist");
  }

  /**
   * Validate JWT secret is configured
   * 
   * Note: JwtProperties already validates this via @PostConstruct,
   * but we log it here for visibility in startup logs
   */
  private void validateJwtSecret() {
    logger.info("Validating JWT configuration...");
    
    String jwtSecret = System.getenv("APP_SECURITY_JWT_SECRET");
    
    if (jwtSecret == null || jwtSecret.isBlank()) {
      throw new IllegalStateException(
          "APP_SECURITY_JWT_SECRET environment variable is not set. " +
          "JWT authentication cannot function without a secret key."
      );
    }
    
    if (jwtSecret.length() < 32) {
      throw new IllegalStateException(
          "APP_SECURITY_JWT_SECRET must be at least 32 characters for security. " +
          "Current length: " + jwtSecret.length()
      );
    }
    
    logger.info("✓ JWT secret configured (length: {} chars)", jwtSecret.length());
  }
}

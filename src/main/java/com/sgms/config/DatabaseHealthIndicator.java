package com.sgms.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database Health Indicator
 * 
 * Custom health check for database connectivity.
 * Runs 'SELECT 1' to verify database is reachable.
 * Returns DOWN if database is unreachable.
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

  private final JdbcTemplate jdbcTemplate;

  public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Health health() {
    try {
      // Execute simple query to test connectivity
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);
      
      return Health.up()
          .withDetail("database", "PostgreSQL")
          .withDetail("status", "Connected")
          .build();
          
    } catch (Exception e) {
      return Health.down()
          .withDetail("database", "PostgreSQL")
          .withDetail("error", "Unable to connect")
          .withDetail("message", e.getMessage())
          .build();
    }
  }
}

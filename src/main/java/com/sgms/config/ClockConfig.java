package com.sgms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Clock Configuration
 * 
 * Provides a centralized Clock bean for time-based operations.
 * All services should inject this Clock instead of using LocalDate.now() or Instant.now().
 * 
 * Benefits:
 * - Explicit timezone handling (UTC)
 * - Testability (can be mocked in tests)
 * - Consistency across all time operations
 */
@Configuration
public class ClockConfig {
  
  /**
   * System clock in UTC timezone
   * 
   * All date/time operations should use this clock to ensure:
   * 1. Consistent timezone (UTC) across all servers
   * 2. Predictable behavior in tests
   * 3. Easy timezone conversion if needed
   */
  @Bean
  public Clock utcClock() {
    return Clock.system(ZoneId.of("UTC"));
  }
}

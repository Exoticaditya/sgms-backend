package com.sgms.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Railway PostgreSQL DataSource Configuration
 * 
 * Parses DATABASE_URL environment variable provided by Railway
 * Format: postgresql://user:password@host:port/database
 * 
 * This configuration ONLY activates when DATABASE_URL is present.
 * Application will FAIL to start if DATABASE_URL is missing in production profile.
 */
@Configuration
@Profile("prod")
public class RailwayPostgresConfig {

  private static final String DATABASE_URL_ENV = "DATABASE_URL";

  @PostConstruct
  public void validateDatabaseUrl() {
    String databaseUrl = System.getenv(DATABASE_URL_ENV);
    if (databaseUrl == null || databaseUrl.isBlank()) {
      throw new IllegalStateException(
          "DATABASE_URL environment variable is required for production deployment. " +
          "Railway provides this automatically. Application cannot start without it."
      );
    }
  }

  @Bean
  @Primary
  public DataSource railwayDataSource() {
    String databaseUrl = System.getenv(DATABASE_URL_ENV);
    
    if (databaseUrl == null || databaseUrl.isBlank()) {
      throw new IllegalStateException(
          "DATABASE_URL environment variable is required for production deployment."
      );
    }

    // Parse DATABASE_URL
    DatabaseCredentials credentials = parseDatabaseUrl(databaseUrl);

    // Build HikariCP configuration
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(credentials.jdbcUrl());
    hikariConfig.setUsername(credentials.username());
    hikariConfig.setPassword(credentials.password());
    hikariConfig.setDriverClassName("org.postgresql.Driver");
    
    // Production-grade connection pool settings
    hikariConfig.setPoolName("SGMSHikariPool");
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(2);
    hikariConfig.setConnectionTimeout(30000);      // 30 seconds
    hikariConfig.setIdleTimeout(600000);           // 10 minutes
    hikariConfig.setMaxLifetime(1800000);          // 30 minutes
    hikariConfig.setLeakDetectionThreshold(60000); // 1 minute
    
    // Connection validation
    hikariConfig.setConnectionTestQuery("SELECT 1");
    hikariConfig.setValidationTimeout(5000);
    
    // PostgreSQL optimizations
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    
    return new HikariDataSource(hikariConfig);
  }

  /**
   * Parses Railway DATABASE_URL into JDBC connection components
   * 
   * @param databaseUrl Railway DATABASE_URL (postgresql://user:password@host:port/database)
   * @return DatabaseCredentials with parsed components
   * @throws IllegalArgumentException if URL format is invalid
   */
  private DatabaseCredentials parseDatabaseUrl(String databaseUrl) {
    try {
      // Replace postgresql:// with jdbc:postgresql://
      if (!databaseUrl.startsWith("postgresql://") && !databaseUrl.startsWith("postgres://")) {
        throw new IllegalArgumentException(
            "DATABASE_URL must start with 'postgresql://' or 'postgres://'"
        );
      }
      
      URI uri = new URI(databaseUrl);
      
      String username = uri.getUserInfo() != null ? uri.getUserInfo().split(":")[0] : null;
      String password = uri.getUserInfo() != null && uri.getUserInfo().contains(":") 
          ? uri.getUserInfo().split(":", 2)[1] 
          : null;
      String host = uri.getHost();
      int port = uri.getPort();
      String database = uri.getPath() != null ? uri.getPath().substring(1) : null; // Remove leading /
      
      if (username == null || password == null || host == null || port == -1 || database == null) {
        throw new IllegalArgumentException(
            "DATABASE_URL is incomplete. Expected format: postgresql://user:password@host:port/database"
        );
      }
      
      String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
      
      return new DatabaseCredentials(jdbcUrl, username, password);
      
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(
          "Invalid DATABASE_URL format. Expected: postgresql://user:password@host:port/database", 
          e
      );
    }
  }

  /**
   * Holds parsed database credentials
   */
  private record DatabaseCredentials(String jdbcUrl, String username, String password) {}
}

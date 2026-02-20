package com.sgms.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
  private String secret;
  private String issuer;
  private long accessTokenTtlSeconds;
  private Environment environment;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @PostConstruct
  public void validate() {
    // In production, JWT secret is REQUIRED
    boolean isProd = environment.matchesProfiles("prod", "production");
    
    if (secret == null || secret.isBlank()) {
      if (isProd) {
        throw new IllegalStateException(
            "JWT secret must be configured in production. Set APP_SECURITY_JWT_SECRET environment variable."
        );
      }
      // Dev fallback warning
      System.err.println("WARNING: Using default JWT secret for development. Never use in production!");
      return;
    }
    
    if (secret.length() < 32) {
      if (isProd) {
        throw new IllegalStateException(
            "JWT secret must be at least 32 characters long for security."
        );
      }
      System.err.println("WARNING: JWT secret is too short. Use at least 32 characters in production.");
    }
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public long getAccessTokenTtlSeconds() {
    return accessTokenTtlSeconds;
  }

  public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
    this.accessTokenTtlSeconds = accessTokenTtlSeconds;
  }
}

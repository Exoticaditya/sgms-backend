package com.sgms.security;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
  private String secret;
  private String issuer;
  private long accessTokenTtlSeconds;

  @PostConstruct
  public void validate() {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException(
          "JWT secret must be configured. Set APP_SECURITY_JWT_SECRET environment variable."
      );
    }
    if (secret.length() < 32) {
      throw new IllegalStateException(
          "JWT secret must be at least 32 characters long for security."
      );
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

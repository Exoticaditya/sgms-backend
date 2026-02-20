package com.sgms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtProperties properties;
  private final Clock clock;
  private SecretKey signingKey;

  public JwtService(JwtProperties properties, Clock clock) {
    this.properties = properties;
    this.clock = clock;
    String secret = properties.getSecret();
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT secret is required (set APP_SECURITY_JWT_SECRET / app.security.jwt.secret)");
    }
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < 32) {
      throw new IllegalStateException("JWT secret must be at least 32 bytes");
    }
    this.signingKey = Keys.hmacShaKeyFor(secretBytes);
  }

  public String generateAccessToken(String subject, String email, Set<String> roleNames) {
    Instant now = clock.instant();
    Instant expiresAt = now.plusSeconds(properties.getAccessTokenTtlSeconds());
    return Jwts.builder()
        .subject(subject)
        .issuer(properties.getIssuer())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .claim("email", email)
        .claim("roles", List.copyOf(roleNames))
        .signWith(signingKey)
        .compact();
  }

  public Jws<Claims> parseAndValidate(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .requireIssuer(properties.getIssuer())
        .build()
        .parseSignedClaims(token);
  }
}

package com.sgms.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
  @Value("${app.cors.allowed-origins:}")
  private String allowedOrigins;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, 
      JwtAuthenticationFilter jwtAuthenticationFilter,
      RestAuthenticationEntryPoint restAuthenticationEntryPoint) throws Exception {
    http
        // API-only: CSRF disabled (stateless JWT authentication)
        .csrf(csrf -> csrf.disable())
        
        // CORS configuration with strict origin whitelist
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        
        // Disable form login and HTTP basic (API uses JWT only)
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        
        // Stateless session management (no server-side sessions)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        // Authorization rules
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
            .requestMatchers("/actuator/health/**", "/actuator/health").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated()
        )
        
        // Exception handling: return JSON for unauthorized requests
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(restAuthenticationEntryPoint)
        )
        
        // JWT filter runs before UsernamePasswordAuthenticationFilter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    
    // Parse and validate allowed origins from configuration
    List<String> origins;
    if (allowedOrigins == null || allowedOrigins.isBlank()) {
      // Production defaults - strict whitelist
      origins = List.of(
          "https://zplusesecurity.com",
          "https://www.zplusesecurity.com"
      );
    } else {
      origins = Arrays.stream(allowedOrigins.split(","))
          .map(String::trim)
          .filter(s -> !s.isBlank() && !s.equals("*"))
          .toList();
    }
    
    // Use setAllowedOriginPatterns to support wildcard patterns like *.netlify.app
    config.setAllowedOriginPatterns(origins);
    
    // Strict method whitelist
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    
    // Only allow necessary headers
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setExposedHeaders(List.of("Authorization"));
    
    // Enable credentials for JWT token transmission
    // WARNING: When credentials=true, wildcard origins are not allowed
    // All origins must be explicitly listed without wildcards
    config.setAllowCredentials(true);
    
    // Apply to all endpoints
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}

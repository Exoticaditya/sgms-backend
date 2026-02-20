package com.sgms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security utility methods for accessing authenticated user context
 */
public class SecurityUtil {

  private SecurityUtil() {
    // Utility class, prevent instantiation
  }

  /**
   * Get the email of the currently authenticated user
   * 
   * @return email of authenticated user
   * @throws IllegalStateException if no user is authenticated
   */
  public static String getCurrentUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }
    
    if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
      return userPrincipal.getUsername(); // getUsername() returns email
    }
    
    // Fallback to username (which should be email in this system)
    return authentication.getName();
  }

  /**
   * Get the UserPrincipal of the currently authenticated user
   * 
   * @return UserPrincipal
   * @throws IllegalStateException if no user is authenticated
   */
  public static UserPrincipal getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }
    
    if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
      return userPrincipal;
    }
    
    throw new IllegalStateException("Principal is not a UserPrincipal");
  }

  /**
   * Get the user ID of the currently authenticated user
   * 
   * @return user ID
   * @throws IllegalStateException if no user is authenticated
   */
  public static Long getCurrentUserId() {
    return getCurrentUser().getUserId();
  }
}

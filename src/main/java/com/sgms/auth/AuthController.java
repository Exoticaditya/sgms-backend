package com.sgms.auth;

import com.sgms.auth.dto.AuthResponse;
import com.sgms.auth.dto.LoginRequest;
import com.sgms.auth.dto.RegisterRequest;
import com.sgms.auth.dto.UserResponse;
import com.sgms.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest request, Authentication authentication) {
    UserPrincipal actor = extractPrincipal(authentication);
    return authService.register(request, actor);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @GetMapping("/me")
  public UserResponse me(Authentication authentication) {
    UserPrincipal principal = extractPrincipal(authentication);
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
    UserResponse response = new UserResponse();
    response.setId(principal.getUserId());
    response.setEmail(principal.getUsername());
    response.setPhone(principal.getUser().getPhone());
    response.setFullName(principal.getUser().getFullName());
    response.setRoles(principal.getRoleNames().stream().sorted().toList());
    return response;
  }

  private UserPrincipal extractPrincipal(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
      return null;
    }
    return principal;
  }
}

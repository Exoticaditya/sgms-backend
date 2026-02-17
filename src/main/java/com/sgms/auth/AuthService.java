package com.sgms.auth;

import com.sgms.auth.dto.AuthResponse;
import com.sgms.auth.dto.LoginRequest;
import com.sgms.auth.dto.RegisterRequest;
import com.sgms.auth.dto.UserResponse;
import com.sgms.security.JwtProperties;
import com.sgms.security.JwtService;
import com.sgms.security.UserPrincipal;
import com.sgms.user.RoleEntity;
import com.sgms.user.RoleRepository;
import com.sgms.user.UserEntity;
import com.sgms.user.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;

  public AuthService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      JwtProperties jwtProperties
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
  }

  @Transactional
  public UserResponse register(RegisterRequest request, UserPrincipal actor) {
    String email = normalizeEmail(request.getEmail());
    if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }

    String requestedRole = request.getRole();
    String roleName = (requestedRole == null || requestedRole.isBlank())
        ? "GUARD"
        : requestedRole.trim().toUpperCase(Locale.ROOT);

    enforceRegistrationPolicy(actor, roleName);

    RoleEntity role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role"));

    UserEntity user = new UserEntity();
    user.setEmail(email);
    user.setPhone(request.getPhone());
    user.setFullName(request.getFullName());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setStatus("ACTIVE");
    user.getRoles().add(role);

    UserEntity saved = userRepository.save(user);
    return toUserResponse(saved);
  }

  public AuthResponse login(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(normalizeEmail(request.getEmail()), request.getPassword()));
    if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    String token = jwtService.generateAccessToken(
        String.valueOf(principal.getUserId()),
        principal.getUsername(),
        principal.getRoleNames()
    );

    AuthResponse response = new AuthResponse();
    response.setAccessToken(token);
    response.setTokenType("Bearer");
    response.setExpiresInSeconds(jwtProperties.getAccessTokenTtlSeconds());
    response.setUser(toUserResponse(principal.getUser()));
    return response;
  }

  private void enforceRegistrationPolicy(UserPrincipal actor, String roleName) {
    if (actor == null) {
      if (!Set.of("GUARD", "CLIENT").contains(roleName)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not allowed for public registration");
      }
      return;
    }

    boolean isAdmin = actor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    boolean isSupervisor = actor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPERVISOR"));

    if (isAdmin) {
      return;
    }
    if (isSupervisor) {
      if (!"GUARD".equals(roleName)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Supervisor can only register GUARD users");
      }
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
  }

  private String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private UserResponse toUserResponse(UserEntity user) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setEmail(user.getEmail());
    response.setPhone(user.getPhone());
    response.setFullName(user.getFullName());
    List<String> roles = user.getRoles().stream().map(RoleEntity::getName).sorted().toList();
    response.setRoles(roles);
    return response;
  }
}

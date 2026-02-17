package com.sgms.security;

import com.sgms.user.RoleEntity;
import com.sgms.user.UserEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
  private final UserEntity user;

  public UserPrincipal(UserEntity user) {
    this.user = user;
  }

  public UserEntity getUser() {
    return user;
  }

  public Long getUserId() {
    return user.getId();
  }

  public Set<String> getRoleNames() {
    return user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
        .map(RoleEntity::getName)
        .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
        .toList();
    return authorities;
  }

  @Override
  public String getPassword() {
    return user.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !"LOCKED".equalsIgnoreCase(user.getStatus());
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return "ACTIVE".equalsIgnoreCase(user.getStatus()) && user.getDeletedAt() == null;
  }
}

package com.sgms.guard;

import com.sgms.guard.dto.CreateGuardRequest;
import com.sgms.guard.dto.GuardResponse;
import com.sgms.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guards")
public class GuardController {

  private final GuardService guardService;

  public GuardController(GuardService guardService) {
    this.guardService = guardService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public GuardResponse createGuard(@Valid @RequestBody CreateGuardRequest request) {
    return guardService.createGuard(request);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public List<GuardResponse> getAllGuards(@AuthenticationPrincipal UserPrincipal principal) {
    return guardService.getAllGuards(principal);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public GuardResponse getGuardById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
    return guardService.getGuardById(id, principal);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public GuardResponse updateGuard(@PathVariable Long id, @Valid @RequestBody CreateGuardRequest request, @AuthenticationPrincipal UserPrincipal principal) {
    return guardService.updateGuard(id, request, principal);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGuard(@PathVariable Long id) {
    guardService.deleteGuard(id);
  }
}

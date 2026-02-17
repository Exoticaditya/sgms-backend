package com.sgms.guard;

import com.sgms.common.ApiResponse;
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
  public ApiResponse<GuardResponse> createGuard(@Valid @RequestBody CreateGuardRequest request) {
    GuardResponse guard = guardService.createGuard(request);
    return ApiResponse.created(guard, "Guard created successfully");
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<GuardResponse>> getAllGuards(@AuthenticationPrincipal UserPrincipal principal) {
    List<GuardResponse> guards = guardService.getAllGuards(principal);
    return ApiResponse.success(guards);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<GuardResponse> getGuardById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
    GuardResponse guard = guardService.getGuardById(id, principal);
    return ApiResponse.success(guard);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<GuardResponse> updateGuard(@PathVariable Long id, @Valid @RequestBody CreateGuardRequest request, @AuthenticationPrincipal UserPrincipal principal) {
    GuardResponse guard = guardService.updateGuard(id, request, principal);
    return ApiResponse.success(guard, "Guard updated successfully");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> deleteGuard(@PathVariable Long id) {
    guardService.deleteGuard(id);
    return ApiResponse.success(null, "Guard deleted successfully");
  }
}

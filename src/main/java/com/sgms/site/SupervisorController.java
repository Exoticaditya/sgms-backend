package com.sgms.site;

import com.sgms.common.ApiResponse;
import com.sgms.site.dto.AssignSupervisorRequest;
import com.sgms.site.dto.SupervisorSiteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for supervisor site assignments
 * 
 * Manages which supervisors oversee which sites.
 * Authorization: ADMIN only for assignments, SUPERVISOR can view own sites
 */
@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

  private final SupervisorSiteService supervisorSiteService;

  public SupervisorController(SupervisorSiteService supervisorSiteService) {
    this.supervisorSiteService = supervisorSiteService;
  }

  /**
   * Assign a supervisor to a site
   * 
   * POST /api/supervisor/assign-site
   * Requires: ADMIN role
   */
  @PostMapping("/assign-site")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SupervisorSiteResponse> assignSupervisor(
      @Valid @RequestBody AssignSupervisorRequest request) {
    SupervisorSiteResponse assignment = supervisorSiteService.assignSupervisor(request);
    return ApiResponse.created(assignment, "Supervisor assigned to site successfully");
  }

  /**
   * Get all sites for a specific supervisor
   * 
   * GET /api/supervisor/sites/{supervisorUserId}
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping("/sites/{supervisorUserId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<SupervisorSiteResponse>> getSitesForSupervisor(
      @PathVariable Long supervisorUserId) {
    List<SupervisorSiteResponse> sites = supervisorSiteService.getSitesForSupervisor(supervisorUserId);
    return ApiResponse.success(sites);
  }

  /**
   * Get all supervisors for a specific site
   * 
   * GET /api/supervisor/site/{siteId}/supervisors
   * Requires: ADMIN role
   */
  @GetMapping("/site/{siteId}/supervisors")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<List<SupervisorSiteResponse>> getSupervisorsForSite(
      @PathVariable Long siteId) {
    List<SupervisorSiteResponse> supervisors = supervisorSiteService.getSupervisorsForSite(siteId);
    return ApiResponse.success(supervisors);
  }

  /**
   * Remove supervisor from a site
   * 
   * DELETE /api/supervisor/remove-site/{supervisorUserId}/{siteId}
   * Requires: ADMIN role
   */
  @DeleteMapping("/remove-site/{supervisorUserId}/{siteId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> removeSupervisorFromSite(
      @PathVariable Long supervisorUserId,
      @PathVariable Long siteId) {
    supervisorSiteService.removeSupervisorFromSite(supervisorUserId, siteId);
    return ApiResponse.success(null, "Supervisor removed from site successfully");
  }
}

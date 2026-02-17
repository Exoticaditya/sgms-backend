package com.sgms.site;

import com.sgms.common.ApiResponse;
import com.sgms.site.dto.CreateSiteRequest;
import com.sgms.site.dto.SiteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for site management
 * 
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/sites")
public class SiteController {

  private final SiteService siteService;

  public SiteController(SiteService siteService) {
    this.siteService = siteService;
  }

  /**
   * Create a new site
   * 
   * POST /api/sites
   * Requires: ADMIN role
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SiteResponse> createSite(@Valid @RequestBody CreateSiteRequest request) {
    SiteResponse site = siteService.createSite(request);
    return ApiResponse.created(site, "Site created successfully");
  }

  /**
   * Get all sites or filter by client account
   * 
   * GET /api/sites
   * GET /api/sites?clientId={id}
   * Requires: ADMIN role
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<List<SiteResponse>> getAllSites(
      @RequestParam(required = false) Long clientId) {
    
    List<SiteResponse> sites;
    if (clientId != null) {
      sites = siteService.getSitesByClientId(clientId);
    } else {
      sites = siteService.getAllSites();
    }
    
    return ApiResponse.success(sites);
  }

  /**
   * Get site by ID
   * 
   * GET /api/sites/{id}
   * Requires: ADMIN role
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<SiteResponse> getSiteById(@PathVariable Long id) {
    SiteResponse site = siteService.getSiteById(id);
    return ApiResponse.success(site);
  }

  /**
   * Delete a site (soft delete)
   * 
   * DELETE /api/sites/{id}
   * Requires: ADMIN role
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> deleteSite(@PathVariable Long id) {
    siteService.deleteSite(id);
    return ApiResponse.success(null, "Site deleted successfully");
  }
}

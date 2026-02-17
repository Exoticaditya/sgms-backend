package com.sgms.site;

import com.sgms.common.ApiResponse;
import com.sgms.site.dto.CreateSitePostRequest;
import com.sgms.site.dto.SitePostResponse;
import com.sgms.site.dto.UpdateSitePostRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for site post management
 * 
 * Site posts are duty stations within a site where guards are assigned.
 * Authorization: ADMIN and SUPERVISOR
 */
@RestController
@RequestMapping("/api/site-posts")
public class SitePostController {

  private final SitePostService sitePostService;

  public SitePostController(SitePostService sitePostService) {
    this.sitePostService = sitePostService;
  }

  /**
   * Create a new site post
   * 
   * POST /api/site-posts
   * Requires: ADMIN or SUPERVISOR role
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SitePostResponse> createSitePost(
      @Valid @RequestBody CreateSitePostRequest request) {
    SitePostResponse post = sitePostService.createSitePost(request);
    return ApiResponse.created(post, "Site post created successfully");
  }

  /**
   * Get all site posts
   * 
   * GET /api/site-posts
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<SitePostResponse>> getAllSitePosts() {
    List<SitePostResponse> posts = sitePostService.getAllSitePosts();
    return ApiResponse.success(posts);
  }

  /**
   * Get site post by ID
   * 
   * GET /api/site-posts/{id}
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<SitePostResponse> getSitePostById(@PathVariable Long id) {
    SitePostResponse post = sitePostService.getSitePostById(id);
    return ApiResponse.success(post);
  }

  /**
   * Get all posts for a specific site
   * 
   * GET /api/site-posts/site/{siteId}
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping("/site/{siteId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<SitePostResponse>> getPostsBySiteId(@PathVariable Long siteId) {
    List<SitePostResponse> posts = sitePostService.getPostsBySiteId(siteId);
    return ApiResponse.success(posts);
  }

  /**
   * Update a site post
   * 
   * PUT /api/site-posts/{id}
   * Requires: ADMIN or SUPERVISOR role
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<SitePostResponse> updateSitePost(
      @PathVariable Long id,
      @Valid @RequestBody UpdateSitePostRequest request) {
    SitePostResponse post = sitePostService.updateSitePost(id, request);
    return ApiResponse.success(post, "Site post updated successfully");
  }

  /**
   * Delete a site post (soft delete)
   * 
   * DELETE /api/site-posts/{id}
   * Requires: ADMIN role
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> deleteSitePost(@PathVariable Long id) {
    sitePostService.deleteSitePost(id);
    return ApiResponse.success(null, "Site post deleted successfully");
  }
}

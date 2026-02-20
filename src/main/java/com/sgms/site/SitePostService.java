package com.sgms.site;

import com.sgms.site.dto.CreateSitePostRequest;
import com.sgms.site.dto.SitePostResponse;
import com.sgms.site.dto.UpdateSitePostRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing site posts
 */
@Service
public class SitePostService {

  private final SitePostRepository sitePostRepository;
  private final SiteRepository siteRepository;
  private final Clock clock;

  public SitePostService(SitePostRepository sitePostRepository, SiteRepository siteRepository, Clock clock) {
    this.sitePostRepository = sitePostRepository;
    this.siteRepository = siteRepository;
    this.clock = clock;
  }

  /**
   * Create a new site post
   */
  @Transactional
  public SitePostResponse createSitePost(CreateSitePostRequest request) {
    // Validate site exists
    SiteEntity site = siteRepository.findActiveById(request.getSiteId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Site not found with id: " + request.getSiteId()
        ));

    // Validate post name is unique for this site
    if (sitePostRepository.existsBySiteIdAndPostNameIgnoreCaseAndActive(
        request.getSiteId(), request.getPostName())) {
      throw new IllegalArgumentException(
          "Post with name '" + request.getPostName() + "' already exists for this site"
      );
    }

    // Create new post
    SitePostEntity post = new SitePostEntity();
    post.setSite(site);
    post.setPostName(request.getPostName());
    post.setDescription(request.getDescription());
    post.setRequiredGuards(request.getRequiredGuards());
    post.setStatus("ACTIVE");

    SitePostEntity saved = sitePostRepository.save(post);
    return mapToResponse(saved);
  }

  /**
   * Get all active site posts
   */
  @Transactional(readOnly = true)
  public List<SitePostResponse> getAllSitePosts() {
    return sitePostRepository.findAllActive()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get site post by ID
   */
  @Transactional(readOnly = true)
  public SitePostResponse getSitePostById(Long id) {
    SitePostEntity post = sitePostRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site post not found with id: " + id
        ));
    return mapToResponse(post);
  }

  /**
   * Get all posts for a specific site
   */
  @Transactional(readOnly = true)
  public List<SitePostResponse> getPostsBySiteId(Long siteId) {
    // Verify site exists
    siteRepository.findActiveById(siteId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site not found with id: " + siteId
        ));

    return sitePostRepository.findAllBySiteId(siteId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Update a site post
   */
  @Transactional
  public SitePostResponse updateSitePost(Long id, UpdateSitePostRequest request) {
    SitePostEntity post = sitePostRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site post not found with id: " + id
        ));

    // Validate post name is unique (excluding current post)
    if (request.getPostName() != null && 
        sitePostRepository.existsBySiteIdAndPostNameIgnoreCaseAndActiveExcludingId(
            post.getSite().getId(), request.getPostName(), id)) {
      throw new IllegalArgumentException(
          "Post with name '" + request.getPostName() + "' already exists for this site"
      );
    }

    // Update fields
    if (request.getPostName() != null) {
      post.setPostName(request.getPostName());
    }
    if (request.getDescription() != null) {
      post.setDescription(request.getDescription());
    }
    if (request.getRequiredGuards() != null) {
      post.setRequiredGuards(request.getRequiredGuards());
    }
    if (request.getStatus() != null) {
      post.setStatus(request.getStatus());
    }

    SitePostEntity updated = sitePostRepository.save(post);
    return mapToResponse(updated);
  }

  /**
   * Soft delete a site post
   */
  @Transactional
  public void deleteSitePost(Long id) {
    SitePostEntity post = sitePostRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site post not found with id: " + id
        ));

    post.setDeletedAt(clock.instant());
    post.setStatus("DELETED");
    sitePostRepository.save(post);
  }

  /**
   * Map entity to response DTO
   */
  private SitePostResponse mapToResponse(SitePostEntity entity) {
    SitePostResponse response = new SitePostResponse();
    response.setId(entity.getId());
    response.setSiteId(entity.getSite().getId());
    response.setSiteName(entity.getSite().getName());
    response.setPostName(entity.getPostName());
    response.setDescription(entity.getDescription());
    response.setRequiredGuards(entity.getRequiredGuards());
    response.setStatus(entity.getStatus());
    response.setCreatedAt(entity.getCreatedAt());
    response.setUpdatedAt(entity.getUpdatedAt());
    response.setDeletedAt(entity.getDeletedAt());
    return response;
  }
}

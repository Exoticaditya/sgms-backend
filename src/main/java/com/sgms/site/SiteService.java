package com.sgms.site;

import com.sgms.client.ClientAccountEntity;
import com.sgms.client.ClientAccountRepository;
import com.sgms.site.dto.CreateSiteRequest;
import com.sgms.site.dto.SiteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing sites
 */
@Service
public class SiteService {

  private final SiteRepository siteRepository;
  private final ClientAccountRepository clientAccountRepository;
  private final Clock clock;

  public SiteService(SiteRepository siteRepository, ClientAccountRepository clientAccountRepository, Clock clock) {
    this.siteRepository = siteRepository;
    this.clientAccountRepository = clientAccountRepository;
    this.clock = clock;
  }

  /**
   * Create a new site
   */
  @Transactional
  public SiteResponse createSite(CreateSiteRequest request) {
    // Validate client account exists
    ClientAccountEntity clientAccount = clientAccountRepository.findActiveById(request.getClientAccountId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Client account not found with id: " + request.getClientAccountId()
        ));

    // Validate site name is unique for this client
    if (siteRepository.existsByClientAccountIdAndNameIgnoreCaseAndActive(
        request.getClientAccountId(), request.getName())) {
      throw new IllegalArgumentException(
          "Site with name '" + request.getName() + "' already exists for this client"
      );
    }

    // Create new site
    SiteEntity site = new SiteEntity();
    site.setClientAccount(clientAccount);
    site.setName(request.getName());
    site.setAddress(request.getAddress());
    site.setLatitude(request.getLatitude());
    site.setLongitude(request.getLongitude());
    site.setStatus("ACTIVE");

    SiteEntity saved = siteRepository.save(site);
    return mapToResponse(saved);
  }

  /**
   * Get all active sites
   */
  @Transactional(readOnly = true)
  public List<SiteResponse> getAllSites() {
    return siteRepository.findAllActive()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get site by ID
   */
  @Transactional(readOnly = true)
  public SiteResponse getSiteById(Long id) {
    SiteEntity site = siteRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site not found with id: " + id
        ));
    return mapToResponse(site);
  }

  /**
   * Get all sites for a specific client account
   */
  @Transactional(readOnly = true)
  public List<SiteResponse> getSitesByClientId(Long clientAccountId) {
    // Verify client exists
    clientAccountRepository.findActiveById(clientAccountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Client account not found with id: " + clientAccountId
        ));

    return siteRepository.findAllByClientAccountId(clientAccountId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Soft delete a site
   * Sets deletedAt timestamp instead of removing from database
   */
  @Transactional
  public void deleteSite(Long id) {
    SiteEntity site = siteRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site not found with id: " + id
        ));

    site.setDeletedAt(clock.instant());
    site.setStatus("DELETED");
    siteRepository.save(site);
  }

  /**
   * Map entity to response DTO
   */
  private SiteResponse mapToResponse(SiteEntity entity) {
    SiteResponse response = new SiteResponse();
    response.setId(entity.getId());
    response.setClientAccountId(entity.getClientAccount().getId());
    response.setClientAccountName(entity.getClientAccount().getName());
    response.setName(entity.getName());
    response.setAddress(entity.getAddress());
    response.setLatitude(entity.getLatitude());
    response.setLongitude(entity.getLongitude());
    response.setStatus(entity.getStatus());
    response.setCreatedAt(entity.getCreatedAt());
    response.setUpdatedAt(entity.getUpdatedAt());
    response.setDeletedAt(entity.getDeletedAt());
    return response;
  }
}

package com.sgms.site;

import com.sgms.site.dto.ClientSiteAccessResponse;
import com.sgms.site.dto.GrantClientAccessRequest;
import com.sgms.user.UserEntity;
import com.sgms.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing client site access
 */
@Service
public class ClientSiteAccessService {

  private final ClientSiteAccessRepository clientSiteAccessRepository;
  private final UserRepository userRepository;
  private final SiteRepository siteRepository;
  private final Clock clock;

  public ClientSiteAccessService(
      ClientSiteAccessRepository clientSiteAccessRepository,
      UserRepository userRepository,
      SiteRepository siteRepository,
      Clock clock) {
    this.clientSiteAccessRepository = clientSiteAccessRepository;
    this.userRepository = userRepository;
    this.siteRepository = siteRepository;
    this.clock = clock;
  }

  /**
   * Grant a client user access to a site
   */
  @Transactional
  public ClientSiteAccessResponse grantAccess(GrantClientAccessRequest request) {
    // Validate client user exists
    UserEntity clientUser = userRepository.findById(request.getClientUserId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Client user not found with id: " + request.getClientUserId()
        ));

    // Validate site exists
    SiteEntity site = siteRepository.findActiveById(request.getSiteId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Site not found with id: " + request.getSiteId()
        ));

    // Check if access already exists
    if (clientSiteAccessRepository.existsActiveAccess(
        request.getClientUserId(), request.getSiteId())) {
      throw new IllegalArgumentException(
          "Client already has access to this site"
      );
    }

    // Create new access grant
    ClientSiteAccessEntity access = new ClientSiteAccessEntity();
    access.setClientUser(clientUser);
    access.setSite(site);

    ClientSiteAccessEntity saved = clientSiteAccessRepository.save(access);
    return mapToResponse(saved);
  }

  /**
   * Get all sites accessible by a specific client
   */
  @Transactional(readOnly = true)
  public List<ClientSiteAccessResponse> getSitesForClient(Long clientUserId) {
    // Verify client exists
    userRepository.findById(clientUserId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Client user not found with id: " + clientUserId
        ));

    return clientSiteAccessRepository.findAllByClientUserId(clientUserId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get all clients with access to a specific site
   */
  @Transactional(readOnly = true)
  public List<ClientSiteAccessResponse> getClientsForSite(Long siteId) {
    // Verify site exists
    siteRepository.findActiveById(siteId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site not found with id: " + siteId
        ));

    return clientSiteAccessRepository.findAllBySiteId(siteId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Revoke client access to a site
   */
  @Transactional
  public void revokeAccess(Long clientUserId, Long siteId) {
    ClientSiteAccessEntity access = clientSiteAccessRepository
        .findActiveAccess(clientUserId, siteId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Active client access not found for client " + 
            clientUserId + " and site " + siteId
        ));

    access.setRevokedAt(clock.instant());
    clientSiteAccessRepository.save(access);
  }

  /**
   * Map entity to response DTO
   */
  private ClientSiteAccessResponse mapToResponse(ClientSiteAccessEntity entity) {
    ClientSiteAccessResponse response = new ClientSiteAccessResponse();
    response.setId(entity.getId());
    response.setClientUserId(entity.getClientUser().getId());
    response.setClientName(entity.getClientUser().getFullName());
    response.setClientEmail(entity.getClientUser().getEmail());
    response.setSiteId(entity.getSite().getId());
    response.setSiteName(entity.getSite().getName());
    response.setGrantedAt(entity.getGrantedAt());
    response.setRevokedAt(entity.getRevokedAt());
    return response;
  }
}

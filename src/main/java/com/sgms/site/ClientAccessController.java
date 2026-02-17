package com.sgms.site;

import com.sgms.common.ApiResponse;
import com.sgms.site.dto.ClientSiteAccessResponse;
import com.sgms.site.dto.GrantClientAccessRequest;
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
 * Controller for client site access management
 * 
 * Manages which client users can access which sites.
 * Authorization: ADMIN only for granting/revoking, CLIENT can view own sites
 */
@RestController
@RequestMapping("/api/client")
public class ClientAccessController {

  private final ClientSiteAccessService clientSiteAccessService;

  public ClientAccessController(ClientSiteAccessService clientSiteAccessService) {
    this.clientSiteAccessService = clientSiteAccessService;
  }

  /**
   * Grant a client user access to a site
   * 
   * POST /api/client/grant-access
   * Requires: ADMIN role
   */
  @PostMapping("/grant-access")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ClientSiteAccessResponse> grantAccess(
      @Valid @RequestBody GrantClientAccessRequest request) {
    ClientSiteAccessResponse access = clientSiteAccessService.grantAccess(request);
    return ApiResponse.created(access, "Client access granted successfully");
  }

  /**
   * Get all sites accessible by a specific client
   * 
   * GET /api/client/sites/{clientUserId}
   * Requires: ADMIN or CLIENT role
   */
  @GetMapping("/sites/{clientUserId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
  public ApiResponse<List<ClientSiteAccessResponse>> getSitesForClient(
      @PathVariable Long clientUserId) {
    List<ClientSiteAccessResponse> sites = clientSiteAccessService.getSitesForClient(clientUserId);
    return ApiResponse.success(sites);
  }

  /**
   * Get all clients with access to a specific site
   * 
   * GET /api/client/site/{siteId}/clients
   * Requires: ADMIN role
   */
  @GetMapping("/site/{siteId}/clients")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<List<ClientSiteAccessResponse>> getClientsForSite(
      @PathVariable Long siteId) {
    List<ClientSiteAccessResponse> clients = clientSiteAccessService.getClientsForSite(siteId);
    return ApiResponse.success(clients);
  }

  /**
   * Revoke client access to a site
   * 
   * DELETE /api/client/revoke-access/{clientUserId}/{siteId}
   * Requires: ADMIN role
   */
  @DeleteMapping("/revoke-access/{clientUserId}/{siteId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> revokeAccess(
      @PathVariable Long clientUserId,
      @PathVariable Long siteId) {
    clientSiteAccessService.revokeAccess(clientUserId, siteId);
    return ApiResponse.success(null, "Client access revoked successfully");
  }
}

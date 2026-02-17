package com.sgms.client;

import com.sgms.client.dto.ClientResponse;
import com.sgms.client.dto.CreateClientRequest;
import com.sgms.common.ApiResponse;
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
 * Controller for client account management
 * 
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/clients")
public class ClientAccountController {

  private final ClientAccountService clientAccountService;

  public ClientAccountController(ClientAccountService clientAccountService) {
    this.clientAccountService = clientAccountService;
  }

  /**
   * Create a new client account
   * 
   * POST /api/clients
   * Requires: ADMIN role
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) {
    ClientResponse client = clientAccountService.createClient(request);
    return ApiResponse.created(client, "Client account created successfully");
  }

  /**
   * Get all client accounts
   * 
   * GET /api/clients
   * Requires: ADMIN role
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<List<ClientResponse>> getAllClients() {
    List<ClientResponse> clients = clientAccountService.getAllClients();
    return ApiResponse.success(clients);
  }

  /**
   * Get client account by ID
   * 
   * GET /api/clients/{id}
   * Requires: ADMIN role
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<ClientResponse> getClientById(@PathVariable Long id) {
    ClientResponse client = clientAccountService.getClientById(id);
    return ApiResponse.success(client);
  }

  /**
   * Delete a client account (soft delete)
   * 
   * DELETE /api/clients/{id}
   * Requires: ADMIN role
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> deleteClient(@PathVariable Long id) {
    clientAccountService.deleteClient(id);
    return ApiResponse.success(null, "Client account deleted successfully");
  }
}

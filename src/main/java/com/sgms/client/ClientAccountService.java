package com.sgms.client;

import com.sgms.client.dto.ClientResponse;
import com.sgms.client.dto.CreateClientRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing client accounts
 */
@Service
public class ClientAccountService {

  private final ClientAccountRepository clientAccountRepository;
  private final Clock clock;

  public ClientAccountService(ClientAccountRepository clientAccountRepository, Clock clock) {
    this.clientAccountRepository = clientAccountRepository;
    this.clock = clock;
  }

  /**
   * Create a new client account
   */
  @Transactional
  public ClientResponse createClient(CreateClientRequest request) {
    // Validate client name is unique
    if (clientAccountRepository.existsByNameIgnoreCaseAndActive(request.getName())) {
      throw new IllegalArgumentException("Client account with name '" + request.getName() + "' already exists");
    }

    // Create new client account
    ClientAccountEntity client = new ClientAccountEntity();
    client.setName(request.getName());
    client.setStatus("ACTIVE");

    ClientAccountEntity saved = clientAccountRepository.save(client);
    return mapToResponse(saved);
  }

  /**
   * Get all active client accounts
   */
  @Transactional(readOnly = true)
  public List<ClientResponse> getAllClients() {
    return clientAccountRepository.findAllActive()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get client account by ID
   */
  @Transactional(readOnly = true)
  public ClientResponse getClientById(Long id) {
    ClientAccountEntity client = clientAccountRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Client account not found with id: " + id
        ));
    return mapToResponse(client);
  }

  /**
   * Soft delete a client account
   * Sets deletedAt timestamp instead of removing from database
   */
  @Transactional
  public void deleteClient(Long id) {
    ClientAccountEntity client = clientAccountRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Client account not found with id: " + id
        ));

    client.setDeletedAt(clock.instant());
    client.setStatus("DELETED");
    clientAccountRepository.save(client);
  }

  /**
   * Map entity to response DTO
   */
  private ClientResponse mapToResponse(ClientAccountEntity entity) {
    ClientResponse response = new ClientResponse();
    response.setId(entity.getId());
    response.setName(entity.getName());
    response.setStatus(entity.getStatus());
    response.setCreatedAt(entity.getCreatedAt());
    response.setDeletedAt(entity.getDeletedAt());
    return response;
  }
}

package com.sgms.site;

import com.sgms.site.dto.AssignSupervisorRequest;
import com.sgms.site.dto.SupervisorSiteResponse;
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
 * Service for managing supervisor site assignments
 */
@Service
public class SupervisorSiteService {

  private final SupervisorSiteMappingRepository supervisorSiteMappingRepository;
  private final UserRepository userRepository;
  private final SiteRepository siteRepository;
  private final Clock clock;

  public SupervisorSiteService(
      SupervisorSiteMappingRepository supervisorSiteMappingRepository,
      UserRepository userRepository,
      SiteRepository siteRepository,
      Clock clock) {
    this.supervisorSiteMappingRepository = supervisorSiteMappingRepository;
    this.userRepository = userRepository;
    this.siteRepository = siteRepository;
    this.clock = clock;
  }

  /**
   * Assign a supervisor to a site
   */
  @Transactional
  public SupervisorSiteResponse assignSupervisor(AssignSupervisorRequest request) {
    // Validate supervisor user exists
    UserEntity supervisor = userRepository.findById(request.getSupervisorUserId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Supervisor user not found with id: " + request.getSupervisorUserId()
        ));

    // Validate site exists
    SiteEntity site = siteRepository.findActiveById(request.getSiteId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Site not found with id: " + request.getSiteId()
        ));

    // Check if assignment already exists
    if (supervisorSiteMappingRepository.existsActiveAssignment(
        request.getSupervisorUserId(), request.getSiteId())) {
      throw new IllegalArgumentException(
          "Supervisor is already assigned to this site"
      );
    }

    // Create new assignment
    SupervisorSiteMappingEntity mapping = new SupervisorSiteMappingEntity();
    mapping.setSupervisor(supervisor);
    mapping.setSite(site);

    SupervisorSiteMappingEntity saved = supervisorSiteMappingRepository.save(mapping);
    return mapToResponse(saved);
  }

  /**
   * Get all sites for a specific supervisor
   */
  @Transactional(readOnly = true)
  public List<SupervisorSiteResponse> getSitesForSupervisor(Long supervisorUserId) {
    // Verify supervisor exists
    userRepository.findById(supervisorUserId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Supervisor user not found with id: " + supervisorUserId
        ));

    return supervisorSiteMappingRepository.findAllBySupervisorId(supervisorUserId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get all supervisors for a specific site
   */
  @Transactional(readOnly = true)
  public List<SupervisorSiteResponse> getSupervisorsForSite(Long siteId) {
    // Verify site exists
    siteRepository.findActiveById(siteId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site not found with id: " + siteId
        ));

    return supervisorSiteMappingRepository.findAllBySiteId(siteId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Remove supervisor from site
   */
  @Transactional
  public void removeSupervisorFromSite(Long supervisorUserId, Long siteId) {
    SupervisorSiteMappingEntity mapping = supervisorSiteMappingRepository
        .findActiveAssignment(supervisorUserId, siteId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Active supervisor assignment not found for supervisor " + 
            supervisorUserId + " and site " + siteId
        ));

    mapping.setRemovedAt(clock.instant());
    supervisorSiteMappingRepository.save(mapping);
  }

  /**
   * Map entity to response DTO
   */
  private SupervisorSiteResponse mapToResponse(SupervisorSiteMappingEntity entity) {
    SupervisorSiteResponse response = new SupervisorSiteResponse();
    response.setId(entity.getId());
    response.setSupervisorUserId(entity.getSupervisor().getId());
    response.setSupervisorName(entity.getSupervisor().getFullName());
    response.setSupervisorEmail(entity.getSupervisor().getEmail());
    response.setSiteId(entity.getSite().getId());
    response.setSiteName(entity.getSite().getName());
    response.setAssignedAt(entity.getAssignedAt());
    response.setRemovedAt(entity.getRemovedAt());
    return response;
  }
}

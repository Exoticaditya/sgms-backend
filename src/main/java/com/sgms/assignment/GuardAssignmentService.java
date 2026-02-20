package com.sgms.assignment;

import com.sgms.assignment.dto.AssignmentResponse;
import com.sgms.assignment.dto.CreateAssignmentRequest;
import com.sgms.guard.GuardEntity;
import com.sgms.guard.GuardRepository;
import com.sgms.security.SecurityUtil;
import com.sgms.site.SitePostEntity;
import com.sgms.site.SitePostRepository;
import com.sgms.user.UserEntity;
import com.sgms.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing guard assignments
 * 
 * Handles guard deployment operations:
 * - Assigning guards to site posts with shifts
 * - Validating assignment constraints
 * - Managing assignment lifecycle
 */
@Service
public class GuardAssignmentService {

  private final GuardAssignmentRepository assignmentRepository;
  private final GuardRepository guardRepository;
  private final SitePostRepository sitePostRepository;
  private final ShiftTypeRepository shiftTypeRepository;
  private final UserRepository userRepository;

  public GuardAssignmentService(
      GuardAssignmentRepository assignmentRepository,
      GuardRepository guardRepository,
      SitePostRepository sitePostRepository,
      ShiftTypeRepository shiftTypeRepository,
      UserRepository userRepository) {
    this.assignmentRepository = assignmentRepository;
    this.guardRepository = guardRepository;
    this.sitePostRepository = sitePostRepository;
    this.shiftTypeRepository = shiftTypeRepository;
    this.userRepository = userRepository;
  }

  /**
   * Create a new guard assignment
   */
  @Transactional
  public AssignmentResponse createAssignment(CreateAssignmentRequest request) {
    // Validate guard exists and is active
    GuardEntity guard = guardRepository.findActiveById(request.getGuardId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Guard not found or inactive with id: " + request.getGuardId()
        ));

    // Validate site post exists and is active
    SitePostEntity sitePost = sitePostRepository.findActiveById(request.getSitePostId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Site post not found or inactive with id: " + request.getSitePostId()
        ));

    // Validate shift type exists
    ShiftTypeEntity shiftType = shiftTypeRepository.findById(request.getShiftTypeId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Shift type not found with id: " + request.getShiftTypeId()
        ));

    // Validate effective dates
    if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
      throw new IllegalArgumentException("Effective to date must be on or after effective from date");
    }

    // Check for overlapping assignments for the same guard
    LocalDate effectiveTo = request.getEffectiveTo() != null ? request.getEffectiveTo() : LocalDate.of(9999, 12, 31);
    if (assignmentRepository.hasOverlappingAssignment(
        request.getGuardId(),
        request.getEffectiveFrom(),
        effectiveTo)) {
      throw new IllegalArgumentException(
          "Guard already has an active assignment during this period. " +
          "Please end or cancel the existing assignment before creating a new one."
      );
    }

    // Get current user (who is creating the assignment)
    String currentUserEmail;
    try {
      currentUserEmail = SecurityUtil.getCurrentUserEmail();
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED,
          "No authenticated user found"
      );
    }
    
    UserEntity createdBy = userRepository.findByEmail(currentUserEmail)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Current user not found"
        ));

    // Create assignment
    GuardAssignmentEntity assignment = new GuardAssignmentEntity();
    assignment.setGuard(guard);
    assignment.setSitePost(sitePost);
    assignment.setShiftType(shiftType);
    assignment.setEffectiveFrom(request.getEffectiveFrom());
    assignment.setEffectiveTo(request.getEffectiveTo());
    assignment.setNotes(request.getNotes());
    assignment.setStatus("ACTIVE");
    assignment.setCreatedBy(createdBy);

    GuardAssignmentEntity saved = assignmentRepository.save(assignment);
    return mapToResponse(saved);
  }

  /**
   * Get all assignments for a specific guard
   */
  @Transactional(readOnly = true)
  public List<AssignmentResponse> getAssignmentsByGuardId(Long guardId) {
    // Verify guard exists
    guardRepository.findById(guardId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Guard not found with id: " + guardId
        ));

    return assignmentRepository.findByGuardId(guardId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get all assignments for a specific site post
   */
  @Transactional(readOnly = true)
  public List<AssignmentResponse> getAssignmentsBySitePostId(Long sitePostId) {
    // Verify site post exists
    sitePostRepository.findById(sitePostId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Site post not found with id: " + sitePostId
        ));

    return assignmentRepository.findBySitePostId(sitePostId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get all active assignments
   */
  @Transactional(readOnly = true)
  public List<AssignmentResponse> getAllActiveAssignments() {
    return assignmentRepository.findAllActiveAssignments()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get assignment by ID
   */
  @Transactional(readOnly = true)
  public AssignmentResponse getAssignmentById(Long id) {
    GuardAssignmentEntity assignment = assignmentRepository.findAssignmentById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Assignment not found with id: " + id
        ));
    return mapToResponse(assignment);
  }

  /**
   * Cancel/Delete an assignment
   * Sets status to CANCELLED (soft delete approach)
   */
  @Transactional
  public void cancelAssignment(Long id) {
    GuardAssignmentEntity assignment = assignmentRepository.findAssignmentById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Assignment not found with id: " + id
        ));

    assignment.setStatus("CANCELLED");
    assignmentRepository.save(assignment);
  }

  /**
   * Map entity to response DTO with denormalized data
   */
  private AssignmentResponse mapToResponse(GuardAssignmentEntity entity) {
    AssignmentResponse response = new AssignmentResponse();
    response.setId(entity.getId());

    // Guard information
    response.setGuardId(entity.getGuard().getId());
    response.setGuardEmployeeCode(entity.getGuard().getEmployeeCode());
    response.setGuardName(entity.getGuard().getFirstName() + " " + 
        (entity.getGuard().getLastName() != null ? entity.getGuard().getLastName() : ""));

    // Site post information
    response.setSitePostId(entity.getSitePost().getId());
    response.setSitePostName(entity.getSitePost().getPostName());
    response.setSiteId(entity.getSitePost().getSite().getId());
    response.setSiteName(entity.getSitePost().getSite().getName());

    // Client information (from site)
    response.setClientId(entity.getSitePost().getSite().getClientAccount().getId());
    response.setClientName(entity.getSitePost().getSite().getClientAccount().getName());

    // Shift type information
    response.setShiftTypeId(entity.getShiftType().getId());
    response.setShiftTypeName(entity.getShiftType().getName());
    response.setShiftStartTime(entity.getShiftType().getStartTime());
    response.setShiftEndTime(entity.getShiftType().getEndTime());

    // Assignment details
    response.setEffectiveFrom(entity.getEffectiveFrom());
    response.setEffectiveTo(entity.getEffectiveTo());
    response.setStatus(entity.getStatus());
    response.setNotes(entity.getNotes());
    response.setCreatedAt(entity.getCreatedAt());
    response.setUpdatedAt(entity.getUpdatedAt());

    // Created by information
    response.setCreatedByUserId(entity.getCreatedBy().getId());
    response.setCreatedByEmail(entity.getCreatedBy().getEmail());

    return response;
  }
}

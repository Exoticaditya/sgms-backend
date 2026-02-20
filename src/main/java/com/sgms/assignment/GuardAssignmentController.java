package com.sgms.assignment;

import com.sgms.assignment.dto.AssignmentResponse;
import com.sgms.assignment.dto.CreateAssignmentRequest;
import com.sgms.assignment.dto.ShiftTypeResponse;
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
 * Controller for guard assignment operations
 * 
 * Manages guard deployment to site posts with shift scheduling.
 * Authorization: ADMIN and SUPERVISOR roles only
 */
@RestController
@RequestMapping("/api/assignments")
public class GuardAssignmentController {

  private final GuardAssignmentService assignmentService;
  private final ShiftTypeService shiftTypeService;

  public GuardAssignmentController(
      GuardAssignmentService assignmentService,
      ShiftTypeService shiftTypeService) {
    this.assignmentService = assignmentService;
    this.shiftTypeService = shiftTypeService;
  }

  /**
   * Create a new guard assignment
   * 
   * POST /api/assignments
   * Requires: ADMIN or SUPERVISOR role
   * 
   * Request body:
   * {
   *   "guardId": 1,
   *   "sitePostId": 2,
   *   "shiftTypeId": 1,
   *   "effectiveFrom": "2026-03-01",
   *   "effectiveTo": "2026-12-31",
   *   "notes": "Main gate assignment"
   * }
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<AssignmentResponse> createAssignment(
      @Valid @RequestBody CreateAssignmentRequest request) {
    AssignmentResponse assignment = assignmentService.createAssignment(request);
    return ApiResponse.success(assignment);
  }

  /**
   * Get all assignments for a specific guard
   * 
   * GET /api/assignments/guard/{guardId}
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping("/guard/{guardId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<AssignmentResponse>> getAssignmentsByGuard(
      @PathVariable Long guardId) {
    List<AssignmentResponse> assignments = assignmentService.getAssignmentsByGuardId(guardId);
    return ApiResponse.success(assignments);
  }

  /**
   * Get all assignments for a specific site post
   * 
   * GET /api/assignments/site-post/{sitePostId}
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping("/site-post/{sitePostId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<AssignmentResponse>> getAssignmentsBySitePost(
      @PathVariable Long sitePostId) {
    List<AssignmentResponse> assignments = assignmentService.getAssignmentsBySitePostId(sitePostId);
    return ApiResponse.success(assignments);
  }

  /**
   * Get all active assignments
   * 
   * GET /api/assignments
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<AssignmentResponse>> getAllActiveAssignments() {
    List<AssignmentResponse> assignments = assignmentService.getAllActiveAssignments();
    return ApiResponse.success(assignments);
  }

  /**
   * Get assignment by ID
   * 
   * GET /api/assignments/{id}
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<AssignmentResponse> getAssignmentById(@PathVariable Long id) {
    AssignmentResponse assignment = assignmentService.getAssignmentById(id);
    return ApiResponse.success(assignment);
  }

  /**
   * Cancel an assignment
   * 
   * DELETE /api/assignments/{id}
   * Requires: ADMIN or SUPERVISOR role
   * 
   * Note: This performs a soft delete by setting status to CANCELLED
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public void cancelAssignment(@PathVariable Long id) {
    assignmentService.cancelAssignment(id);
  }

  /**
   * Get all shift types (for dropdown selection)
   * 
   * GET /api/assignments/shift-types
   * Requires: ADMIN or SUPERVISOR role
   */
  @GetMapping("/shift-types")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
  public ApiResponse<List<ShiftTypeResponse>> getAllShiftTypes() {
    List<ShiftTypeResponse> shiftTypes = shiftTypeService.getAllShiftTypes();
    return ApiResponse.success(shiftTypes);
  }
}

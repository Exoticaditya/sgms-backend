package com.sgms.assignment;

import com.sgms.assignment.dto.ShiftTypeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing shift types
 * 
 * Shift types are lookup/reference data (DAY, NIGHT, EVENING)
 * Typically seeded during initial migration and rarely modified
 */
@Service
public class ShiftTypeService {

  private final ShiftTypeRepository shiftTypeRepository;

  public ShiftTypeService(ShiftTypeRepository shiftTypeRepository) {
    this.shiftTypeRepository = shiftTypeRepository;
  }

  /**
   * Get all shift types ordered by start time
   */
  @Transactional(readOnly = true)
  public List<ShiftTypeResponse> getAllShiftTypes() {
    return shiftTypeRepository.findAllOrderedByStartTime()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get shift type by ID
   */
  @Transactional(readOnly = true)
  public ShiftTypeResponse getShiftTypeById(Long id) {
    ShiftTypeEntity entity = shiftTypeRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Shift type not found with id: " + id));
    return mapToResponse(entity);
  }

  /**
   * Get shift type by name (case-insensitive)
   */
  @Transactional(readOnly = true)
  public ShiftTypeResponse getShiftTypeByName(String name) {
    ShiftTypeEntity entity = shiftTypeRepository.findByNameIgnoreCase(name)
        .orElseThrow(() -> new IllegalArgumentException("Shift type not found with name: " + name));
    return mapToResponse(entity);
  }

  /**
   * Map entity to response DTO
   */
  private ShiftTypeResponse mapToResponse(ShiftTypeEntity entity) {
    ShiftTypeResponse response = new ShiftTypeResponse();
    response.setId(entity.getId());
    response.setName(entity.getName());
    response.setStartTime(entity.getStartTime());
    response.setEndTime(entity.getEndTime());
    response.setDescription(entity.getDescription());
    response.setCreatedAt(entity.getCreatedAt());
    return response;
  }
}

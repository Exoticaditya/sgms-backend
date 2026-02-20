package com.sgms.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ShiftType entity
 */
@Repository
public interface ShiftTypeRepository extends JpaRepository<ShiftTypeEntity, Long> {

  /**
   * Find shift type by name (case-insensitive)
   */
  @Query("SELECT st FROM ShiftTypeEntity st WHERE UPPER(st.name) = UPPER(:name)")
  Optional<ShiftTypeEntity> findByNameIgnoreCase(String name);

  /**
   * Find all shift types ordered by start time
   */
  @Query("SELECT st FROM ShiftTypeEntity st ORDER BY st.startTime")
  List<ShiftTypeEntity> findAllOrderedByStartTime();

  /**
   * Check if shift type name exists
   */
  @Query("SELECT COUNT(st) > 0 FROM ShiftTypeEntity st WHERE UPPER(st.name) = UPPER(:name)")
  boolean existsByNameIgnoreCase(String name);
}

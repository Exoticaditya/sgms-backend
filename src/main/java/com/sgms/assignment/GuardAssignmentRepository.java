package com.sgms.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GuardAssignment entity
 */
@Repository
public interface GuardAssignmentRepository extends JpaRepository<GuardAssignmentEntity, Long> {

  /**
   * Find all assignments for a specific guard
   * Ordered by effective_from descending (most recent first)
   */
  @Query("SELECT ga FROM GuardAssignmentEntity ga WHERE ga.guard.id = :guardId ORDER BY ga.effectiveFrom DESC")
  List<GuardAssignmentEntity> findByGuardId(Long guardId);

  /**
   * Find all assignments for a specific site post
   * Ordered by effective_from descending
   */
  @Query("SELECT ga FROM GuardAssignmentEntity ga WHERE ga.sitePost.id = :sitePostId ORDER BY ga.effectiveFrom DESC")
  List<GuardAssignmentEntity> findBySitePostId(Long sitePostId);

  /**
   * Find active assignments for a guard
   * Active = status is ACTIVE AND current date is within effective date range
   */
  @Query("SELECT ga FROM GuardAssignmentEntity ga " +
         "WHERE ga.guard.id = :guardId " +
         "AND ga.status = 'ACTIVE' " +
         "AND ga.effectiveFrom <= :currentDate " +
         "AND (ga.effectiveTo IS NULL OR ga.effectiveTo >= :currentDate) " +
         "ORDER BY ga.effectiveFrom DESC")
  List<GuardAssignmentEntity> findActiveAssignmentsByGuardId(Long guardId, LocalDate currentDate);

  /**
   * Find active assignments for a site post
   * Active = status is ACTIVE AND current date is within effective date range
   */
  @Query("SELECT ga FROM GuardAssignmentEntity ga " +
         "WHERE ga.sitePost.id = :sitePostId " +
         "AND ga.status = 'ACTIVE' " +
         "AND ga.effectiveFrom <= :currentDate " +
         "AND (ga.effectiveTo IS NULL OR ga.effectiveTo >= :currentDate) " +
         "ORDER BY ga.effectiveFrom DESC")
  List<GuardAssignmentEntity> findActiveAssignmentsBySitePostId(Long sitePostId, LocalDate currentDate);

  /**
   * Find assignment by ID
   */
  @Query("SELECT ga FROM GuardAssignmentEntity ga WHERE ga.id = :id")
  Optional<GuardAssignmentEntity> findAssignmentById(Long id);

  /**
   * Find all active assignments (status = ACTIVE)
   */
  @Query("SELECT ga FROM GuardAssignmentEntity ga WHERE ga.status = 'ACTIVE' ORDER BY ga.effectiveFrom DESC")
  List<GuardAssignmentEntity> findAllActiveAssignments();

  /**
   * Check for overlapping assignments for the same guard
   * Used to prevent double-booking a guard at the same time
   */
  @Query("SELECT COUNT(ga) > 0 FROM GuardAssignmentEntity ga " +
         "WHERE ga.guard.id = :guardId " +
         "AND ga.status = 'ACTIVE' " +
         "AND ga.id != :excludeId " +
         "AND ga.effectiveFrom <= :effectiveTo " +
         "AND (ga.effectiveTo IS NULL OR ga.effectiveTo >= :effectiveFrom)")
  boolean hasOverlappingAssignment(Long guardId, LocalDate effectiveFrom, LocalDate effectiveTo, Long excludeId);

  /**
   * Overload for creating new assignments (no excludeId)
   */
  default boolean hasOverlappingAssignment(Long guardId, LocalDate effectiveFrom, LocalDate effectiveTo) {
    LocalDate endDate = effectiveTo != null ? effectiveTo : LocalDate.of(9999, 12, 31);
    return hasOverlappingAssignment(guardId, effectiveFrom, endDate, -1L);
  }
}

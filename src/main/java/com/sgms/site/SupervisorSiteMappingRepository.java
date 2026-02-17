package com.sgms.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SupervisorSiteMapping entity
 */
@Repository
public interface SupervisorSiteMappingRepository extends JpaRepository<SupervisorSiteMappingEntity, Long> {

  /**
   * Find all active supervisor site assignments
   */
  @Query("SELECT ssm FROM SupervisorSiteMappingEntity ssm WHERE ssm.removedAt IS NULL ORDER BY ssm.assignedAt DESC")
  List<SupervisorSiteMappingEntity> findAllActive();

  /**
   * Find all active sites for a specific supervisor
   */
  @Query("SELECT ssm FROM SupervisorSiteMappingEntity ssm WHERE ssm.supervisor.id = :supervisorUserId AND ssm.removedAt IS NULL ORDER BY ssm.assignedAt DESC")
  List<SupervisorSiteMappingEntity> findAllBySupervisorId(Long supervisorUserId);

  /**
   * Find all active supervisors for a specific site
   */
  @Query("SELECT ssm FROM SupervisorSiteMappingEntity ssm WHERE ssm.site.id = :siteId AND ssm.removedAt IS NULL ORDER BY ssm.assignedAt DESC")
  List<SupervisorSiteMappingEntity> findAllBySiteId(Long siteId);

  /**
   * Find active assignment by supervisor and site
   */
  @Query("SELECT ssm FROM SupervisorSiteMappingEntity ssm WHERE ssm.supervisor.id = :supervisorUserId AND ssm.site.id = :siteId AND ssm.removedAt IS NULL")
  Optional<SupervisorSiteMappingEntity> findActiveAssignment(Long supervisorUserId, Long siteId);

  /**
   * Check if active assignment exists
   */
  @Query("SELECT COUNT(ssm) > 0 FROM SupervisorSiteMappingEntity ssm WHERE ssm.supervisor.id = :supervisorUserId AND ssm.site.id = :siteId AND ssm.removedAt IS NULL")
  boolean existsActiveAssignment(Long supervisorUserId, Long siteId);
}

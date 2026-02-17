package com.sgms.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Site entity
 */
@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Long> {

  /**
   * Find all active (non-deleted) sites
   */
  @Query("SELECT s FROM SiteEntity s WHERE s.deletedAt IS NULL ORDER BY s.createdAt DESC")
  List<SiteEntity> findAllActive();

  /**
   * Find active site by ID
   */
  @Query("SELECT s FROM SiteEntity s WHERE s.id = :id AND s.deletedAt IS NULL")
  Optional<SiteEntity> findActiveById(Long id);

  /**
   * Find all active sites for a specific client account
   */
  @Query("SELECT s FROM SiteEntity s WHERE s.clientAccount.id = :clientAccountId AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
  List<SiteEntity> findAllByClientAccountId(Long clientAccountId);

  /**
   * Check if site name exists for a client (case-insensitive, active only)
   */
  @Query("SELECT COUNT(s) > 0 FROM SiteEntity s WHERE s.clientAccount.id = :clientAccountId AND LOWER(s.name) = LOWER(:name) AND s.deletedAt IS NULL")
  boolean existsByClientAccountIdAndNameIgnoreCaseAndActive(Long clientAccountId, String name);
}

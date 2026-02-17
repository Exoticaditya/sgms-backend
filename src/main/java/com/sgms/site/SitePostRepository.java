package com.sgms.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SitePost entity
 */
@Repository
public interface SitePostRepository extends JpaRepository<SitePostEntity, Long> {

  /**
   * Find all active (non-deleted) site posts
   */
  @Query("SELECT sp FROM SitePostEntity sp WHERE sp.deletedAt IS NULL ORDER BY sp.createdAt DESC")
  List<SitePostEntity> findAllActive();

  /**
   * Find active site post by ID
   */
  @Query("SELECT sp FROM SitePostEntity sp WHERE sp.id = :id AND sp.deletedAt IS NULL")
  Optional<SitePostEntity> findActiveById(Long id);

  /**
   * Find all active posts for a specific site
   */
  @Query("SELECT sp FROM SitePostEntity sp WHERE sp.site.id = :siteId AND sp.deletedAt IS NULL ORDER BY sp.createdAt DESC")
  List<SitePostEntity> findAllBySiteId(Long siteId);

  /**
   * Check if post name exists for a site (case-insensitive, active only)
   */
  @Query("SELECT COUNT(sp) > 0 FROM SitePostEntity sp WHERE sp.site.id = :siteId AND LOWER(sp.postName) = LOWER(:postName) AND sp.deletedAt IS NULL")
  boolean existsBySiteIdAndPostNameIgnoreCaseAndActive(Long siteId, String postName);

  /**
   * Check if post name exists for a site excluding specific post ID (for updates)
   */
  @Query("SELECT COUNT(sp) > 0 FROM SitePostEntity sp WHERE sp.site.id = :siteId AND LOWER(sp.postName) = LOWER(:postName) AND sp.id != :excludeId AND sp.deletedAt IS NULL")
  boolean existsBySiteIdAndPostNameIgnoreCaseAndActiveExcludingId(Long siteId, String postName, Long excludeId);
}

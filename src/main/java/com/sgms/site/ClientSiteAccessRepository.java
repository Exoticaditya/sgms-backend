package com.sgms.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ClientSiteAccess entity
 */
@Repository
public interface ClientSiteAccessRepository extends JpaRepository<ClientSiteAccessEntity, Long> {

  /**
   * Find all active client site access grants
   */
  @Query("SELECT csa FROM ClientSiteAccessEntity csa WHERE csa.revokedAt IS NULL ORDER BY csa.grantedAt DESC")
  List<ClientSiteAccessEntity> findAllActive();

  /**
   * Find all active sites accessible by a specific client
   */
  @Query("SELECT csa FROM ClientSiteAccessEntity csa WHERE csa.clientUser.id = :clientUserId AND csa.revokedAt IS NULL ORDER BY csa.grantedAt DESC")
  List<ClientSiteAccessEntity> findAllByClientUserId(Long clientUserId);

  /**
   * Find all active clients with access to a specific site
   */
  @Query("SELECT csa FROM ClientSiteAccessEntity csa WHERE csa.site.id = :siteId AND csa.revokedAt IS NULL ORDER BY csa.grantedAt DESC")
  List<ClientSiteAccessEntity> findAllBySiteId(Long siteId);

  /**
   * Find active access grant by client and site
   */
  @Query("SELECT csa FROM ClientSiteAccessEntity csa WHERE csa.clientUser.id = :clientUserId AND csa.site.id = :siteId AND csa.revokedAt IS NULL")
  Optional<ClientSiteAccessEntity> findActiveAccess(Long clientUserId, Long siteId);

  /**
   * Check if active access exists
   */
  @Query("SELECT COUNT(csa) > 0 FROM ClientSiteAccessEntity csa WHERE csa.clientUser.id = :clientUserId AND csa.site.id = :siteId AND csa.revokedAt IS NULL")
  boolean existsActiveAccess(Long clientUserId, Long siteId);
}

package com.sgms.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ClientAccount entity
 */
@Repository
public interface ClientAccountRepository extends JpaRepository<ClientAccountEntity, Long> {

  /**
   * Find all active (non-deleted) client accounts
   */
  @Query("SELECT c FROM ClientAccountEntity c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<ClientAccountEntity> findAllActive();

  /**
   * Find active client account by ID
   */
  @Query("SELECT c FROM ClientAccountEntity c WHERE c.id = :id AND c.deletedAt IS NULL")
  Optional<ClientAccountEntity> findActiveById(Long id);

  /**
   * Check if client name already exists (case-insensitive, active only)
   */
  @Query("SELECT COUNT(c) > 0 FROM ClientAccountEntity c WHERE LOWER(c.name) = LOWER(:name) AND c.deletedAt IS NULL")
  boolean existsByNameIgnoreCaseAndActive(String name);
}

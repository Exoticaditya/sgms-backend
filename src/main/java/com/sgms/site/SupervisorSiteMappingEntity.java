package com.sgms.site;

import com.sgms.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * SupervisorSiteMapping Entity
 * 
 * Maps supervisors to sites they oversee.
 * A supervisor can manage multiple sites.
 * 
 * Table: supervisor_site_mapping
 */
@Entity
@Table(name = "supervisor_site_mapping")
public class SupervisorSiteMappingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "supervisor_user_id", nullable = false)
  private UserEntity supervisor;

  @ManyToOne
  @JoinColumn(name = "site_id", nullable = false)
  private SiteEntity site;

  @Column(name = "assigned_at", nullable = false)
  private Instant assignedAt;

  @Column(name = "removed_at")
  private Instant removedAt;

  @PrePersist
  public void prePersist() {
    if (assignedAt == null) {
      assignedAt = Instant.now();
    }
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UserEntity getSupervisor() {
    return supervisor;
  }

  public void setSupervisor(UserEntity supervisor) {
    this.supervisor = supervisor;
  }

  public SiteEntity getSite() {
    return site;
  }

  public void setSite(SiteEntity site) {
    this.site = site;
  }

  public Instant getAssignedAt() {
    return assignedAt;
  }

  public void setAssignedAt(Instant assignedAt) {
    this.assignedAt = assignedAt;
  }

  public Instant getRemovedAt() {
    return removedAt;
  }

  public void setRemovedAt(Instant removedAt) {
    this.removedAt = removedAt;
  }
}

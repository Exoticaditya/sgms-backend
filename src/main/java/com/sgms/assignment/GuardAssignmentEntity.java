package com.sgms.assignment;

import com.sgms.guard.GuardEntity;
import com.sgms.site.SitePostEntity;
import com.sgms.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/**
 * GuardAssignment Entity
 * 
 * Represents a guard deployment assignment to a site post.
 * Maps guards to specific posts with shift type and effective date range.
 * 
 * Table: guard_assignments
 */
@Entity
@Table(name = "guard_assignments")
public class GuardAssignmentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "guard_id", nullable = false)
  private GuardEntity guard;

  @ManyToOne
  @JoinColumn(name = "site_post_id", nullable = false)
  private SitePostEntity sitePost;

  @ManyToOne
  @JoinColumn(name = "shift_type_id", nullable = false)
  private ShiftTypeEntity shiftType;

  @Column(name = "effective_from", nullable = false)
  private LocalDate effectiveFrom;

  @Column(name = "effective_to")
  private LocalDate effectiveTo;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToOne
  @JoinColumn(name = "created_by_user_id", nullable = false)
  private UserEntity createdBy;

  @PrePersist
  public void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
    if (status == null) {
      status = "ACTIVE";
    }
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Instant.now();
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public GuardEntity getGuard() {
    return guard;
  }

  public void setGuard(GuardEntity guard) {
    this.guard = guard;
  }

  public SitePostEntity getSitePost() {
    return sitePost;
  }

  public void setSitePost(SitePostEntity sitePost) {
    this.sitePost = sitePost;
  }

  public ShiftTypeEntity getShiftType() {
    return shiftType;
  }

  public void setShiftType(ShiftTypeEntity shiftType) {
    this.shiftType = shiftType;
  }

  public LocalDate getEffectiveFrom() {
    return effectiveFrom;
  }

  public void setEffectiveFrom(LocalDate effectiveFrom) {
    this.effectiveFrom = effectiveFrom;
  }

  public LocalDate getEffectiveTo() {
    return effectiveTo;
  }

  public void setEffectiveTo(LocalDate effectiveTo) {
    this.effectiveTo = effectiveTo;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public UserEntity getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UserEntity createdBy) {
    this.createdBy = createdBy;
  }
}

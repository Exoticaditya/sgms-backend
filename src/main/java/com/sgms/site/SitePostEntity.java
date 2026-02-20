package com.sgms.site;

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

/**
 * SitePost Entity
 * 
 * Represents a guard duty post/station within a site.
 * Examples: "Main Gate", "Lobby", "Parking Area"
 * 
 * Table: site_posts
 */
@Entity
@Table(name = "site_posts")
public class SitePostEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "site_id", nullable = false)
  private SiteEntity site;

  @Column(name = "post_name", nullable = false, length = 255)
  private String postName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "required_guards", nullable = false)
  private Integer requiredGuards = 1;

  @Column(name = "active", nullable = false)
  private Boolean active = true;

  @Column(name = "status", length = 20, nullable = false)
  private String status = "ACTIVE";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @PrePersist
  public void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
    if (active == null) {
      active = true;
    }
    if (requiredGuards == null) {
      requiredGuards = 1;
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

  public SiteEntity getSite() {
    return site;
  }

  public void setSite(SiteEntity site) {
    this.site = site;
  }

  public String getPostName() {
    return postName;
  }

  public void setPostName(String postName) {
    this.postName = postName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getRequiredGuards() {
    return requiredGuards;
  }

  public void setRequiredGuards(Integer requiredGuards) {
    this.requiredGuards = requiredGuards;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
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

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}

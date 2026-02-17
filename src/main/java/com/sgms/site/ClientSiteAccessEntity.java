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
 * ClientSiteAccess Entity
 * 
 * Grants client users access to view specific sites.
 * Used for client portal access control.
 * 
 * Table: client_site_access
 */
@Entity
@Table(name = "client_site_access")
public class ClientSiteAccessEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "client_user_id", nullable = false)
  private UserEntity clientUser;

  @ManyToOne
  @JoinColumn(name = "site_id", nullable = false)
  private SiteEntity site;

  @Column(name = "granted_at", nullable = false)
  private Instant grantedAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @PrePersist
  public void prePersist() {
    if (grantedAt == null) {
      grantedAt = Instant.now();
    }
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UserEntity getClientUser() {
    return clientUser;
  }

  public void setClientUser(UserEntity clientUser) {
    this.clientUser = clientUser;
  }

  public SiteEntity getSite() {
    return site;
  }

  public void setSite(SiteEntity site) {
    this.site = site;
  }

  public Instant getGrantedAt() {
    return grantedAt;
  }

  public void setGrantedAt(Instant grantedAt) {
    this.grantedAt = grantedAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }
}

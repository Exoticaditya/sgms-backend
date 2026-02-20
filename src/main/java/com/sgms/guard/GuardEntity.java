package com.sgms.guard;

import com.sgms.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "guards")
public class GuardEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserEntity user;

  @ManyToOne
  @JoinColumn(name = "supervisor_user_id")
  private UserEntity supervisor;

  @Column(name = "employee_code", nullable = false, length = 50, unique = true)
  private String employeeCode;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  @Column(name = "phone", length = 30)
  private String phone;

  @Column(name = "active", nullable = false)
  private Boolean active = true;

  @Column(name = "status", length = 20, nullable = false)
  private String status = "ACTIVE";

  @Column(name = "hire_date")
  private LocalDate hireDate;

  @Column(name = "base_salary", nullable = false)
  private BigDecimal baseSalary;

  @Column(name = "per_day_rate", nullable = false)
  private BigDecimal perDayRate;

  @Column(name = "overtime_rate", nullable = false)
  private BigDecimal overtimeRate;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @PrePersist
  public void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
    if (active == null) active = true;
    if (baseSalary == null) baseSalary = BigDecimal.ZERO;
    if (perDayRate == null) perDayRate = BigDecimal.ZERO;
    if (overtimeRate == null) overtimeRate = BigDecimal.ZERO;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Instant.now();
  }

  // Getters and Setters

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public UserEntity getUser() { return user; }
  public void setUser(UserEntity user) { this.user = user; }

  public UserEntity getSupervisor() { return supervisor; }
  public void setSupervisor(UserEntity supervisor) { this.supervisor = supervisor; }

  public String getEmployeeCode() { return employeeCode; }
  public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }

  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public Boolean getActive() { return active; }
  public void setActive(Boolean active) { this.active = active; }

  public LocalDate getHireDate() { return hireDate; }
  public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

  public BigDecimal getBaseSalary() { return baseSalary; }
  public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }

  public BigDecimal getPerDayRate() { return perDayRate; }
  public void setPerDayRate(BigDecimal perDayRate) { this.perDayRate = perDayRate; }

  public BigDecimal getOvertimeRate() { return overtimeRate; }
  public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

  public Instant getDeletedAt() { return deletedAt; }
  public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}

package com.sgms.guard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GuardResponse {
  private Long id;
  private Long userId;
  private String email;
  private Long supervisorId;
  private String supervisorName;
  private String employeeCode;
  private String firstName;
  private String lastName;
  private String phone;
  private String status;
  private LocalDate hireDate;
  private BigDecimal baseSalary;
  private BigDecimal perDayRate;
  private BigDecimal overtimeRate;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public Long getSupervisorId() { return supervisorId; }
  public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

  public String getSupervisorName() { return supervisorName; }
  public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

  public String getEmployeeCode() { return employeeCode; }
  public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }

  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public LocalDate getHireDate() { return hireDate; }
  public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

  public BigDecimal getBaseSalary() { return baseSalary; }
  public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }

  public BigDecimal getPerDayRate() { return perDayRate; }
  public void setPerDayRate(BigDecimal perDayRate) { this.perDayRate = perDayRate; }

  public BigDecimal getOvertimeRate() { return overtimeRate; }
  public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }
}

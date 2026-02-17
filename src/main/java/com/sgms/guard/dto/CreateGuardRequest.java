package com.sgms.guard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGuardRequest {
  @NotBlank
  @Email
  private String email;

  @NotBlank
  private String password;

  @NotBlank
  private String firstName;

  private String lastName;

  private String phone;

  @NotBlank
  private String employeeCode;

  private Long supervisorId;

  private LocalDate hireDate;

  @NotNull
  private BigDecimal baseSalary;

  @NotNull
  private BigDecimal perDayRate;

  @NotNull
  private BigDecimal overtimeRate;

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }

  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getEmployeeCode() { return employeeCode; }
  public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

  public Long getSupervisorId() { return supervisorId; }
  public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

  public LocalDate getHireDate() { return hireDate; }
  public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

  public BigDecimal getBaseSalary() { return baseSalary; }
  public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }

  public BigDecimal getPerDayRate() { return perDayRate; }
  public void setPerDayRate(BigDecimal perDayRate) { this.perDayRate = perDayRate; }

  public BigDecimal getOvertimeRate() { return overtimeRate; }
  public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }
}

package com.sgms.guard;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuardRepository extends JpaRepository<GuardEntity, Long> {
  
  @Query("SELECT g FROM GuardEntity g WHERE g.deletedAt IS NULL")
  List<List<GuardEntity>> findAllActive();

  @Query("SELECT g FROM GuardEntity g WHERE g.supervisor.id = :supervisorId AND g.deletedAt IS NULL")
  List<GuardEntity> findBySupervisorId(@Param("supervisorId") Long supervisorId);

  @Query("SELECT g FROM GuardEntity g WHERE g.id = :id AND g.deletedAt IS NULL")
  Optional<GuardEntity> findByIdActive(@Param("id") Long id);

  boolean existsByEmployeeCode(String employeeCode);
}

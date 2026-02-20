package com.sgms.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
  Optional<UserEntity> findByEmail(String email);
  boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);
}

package com.sgms.seed;

import com.sgms.user.RoleEntity;
import com.sgms.user.RoleRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleSeeder implements ApplicationRunner {
  private final RoleRepository roleRepository;

  public RoleSeeder(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    List<String> roleNames = List.of("ADMIN", "SUPERVISOR", "GUARD", "CLIENT");
    for (String roleName : roleNames) {
      roleRepository.findByName(roleName).orElseGet(() -> {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        return roleRepository.save(role);
      });
    }
  }
}

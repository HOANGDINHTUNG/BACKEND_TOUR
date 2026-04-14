package com.wedservice.backend.module.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wedservice.backend.module.users.entity.Role;
import java.util.Optional;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    List<Role> findAllByCodeIn(java.util.Collection<String> codes);
}

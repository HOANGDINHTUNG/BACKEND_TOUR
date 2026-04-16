package com.wedservice.backend.module.users.repository;

import com.wedservice.backend.module.users.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findAllByOrderByModuleNameAscActionNameAscNameAsc();
    List<Permission> findAllByCodeIn(java.util.Collection<String> codes);
}

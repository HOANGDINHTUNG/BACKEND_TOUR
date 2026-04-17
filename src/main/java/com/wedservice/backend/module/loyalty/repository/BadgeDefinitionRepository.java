package com.wedservice.backend.module.loyalty.repository;

import com.wedservice.backend.module.loyalty.entity.BadgeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeDefinitionRepository extends JpaRepository<BadgeDefinition, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<BadgeDefinition> findAllByOrderByCreatedAtDesc();
}

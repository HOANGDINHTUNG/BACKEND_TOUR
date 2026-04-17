package com.wedservice.backend.module.loyalty.repository;

import com.wedservice.backend.module.loyalty.entity.MissionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MissionDefinitionRepository extends JpaRepository<MissionDefinition, Long>, JpaSpecificationExecutor<MissionDefinition> {
    Optional<MissionDefinition> findByCode(String code);
}

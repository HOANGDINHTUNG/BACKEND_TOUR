package com.wedservice.backend.module.commerce.repository;

import com.wedservice.backend.module.commerce.entity.ComboPackage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ComboPackageRepository extends JpaRepository<ComboPackage, Long>, JpaSpecificationExecutor<ComboPackage> {

    Optional<ComboPackage> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @EntityGraph(attributePaths = "items")
    @Query("select c from ComboPackage c where c.id = :id")
    Optional<ComboPackage> findDetailById(@Param("id") Long id);
}

package com.wedservice.backend.module.destinations.repository;

import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long>, QuerydslPredicateExecutor<Destination> {

    Optional<Destination> findByUuid(UUID uuid);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    boolean existsByNameIgnoreCaseAndProvinceIgnoreCaseAndStatusIn(
            String name,
            String province,
            Collection<DestinationStatus> statuses
    );

}

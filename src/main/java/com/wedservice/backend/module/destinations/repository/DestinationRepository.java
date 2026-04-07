package com.wedservice.backend.module.destinations.repository;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findByUuid(UUID uuid);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    @Query("""
            SELECT d FROM Destination d
            WHERE (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:province IS NULL OR d.province = :province)
                AND (:region IS NULL OR d.region = :region)
                AND (:crowdLevel IS NULL OR d.crowdLevelDefault = :crowdLevel)
                AND (:isFeatured IS NULL OR d.isFeatured = :isFeatured)
                AND (:isActive IS NULL OR d.isActive = :isActive)
                AND (:status IS NULL OR d.status = :status)
                AND (:isOfficial IS NULL OR d.isOfficial = :isOfficial)
            """)
    Page<Destination> searchDestinations(
            @Param("keyword") String keyword,
            @Param("province") String province,
            @Param("region") String region,
            @Param("crowdLevel") CrowdLevel crowdLevel,
            @Param("isFeatured") Boolean isFeatured,
            @Param("isActive") Boolean isActive,
            @Param("status") DestinationStatus status,
            @Param("isOfficial") Boolean isOfficial,
            Pageable pageable
    );
}

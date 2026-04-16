package com.wedservice.backend.module.users.repository;

import com.wedservice.backend.module.users.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdOrderByIsDefaultDescIdAsc(UUID userId);

    Optional<UserAddress> findByIdAndUserId(Long id, UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByUserIdAndIdNot(UUID userId, Long id);

    Optional<UserAddress> findFirstByUserIdOrderByIdAsc(UUID userId);

    @Modifying
    @Query("""
            update UserAddress ua
            set ua.isDefault = false
            where ua.userId = :userId
              and (:excludeId is null or ua.id <> :excludeId)
            """)
    void clearDefaultByUserId(@Param("userId") UUID userId, @Param("excludeId") Long excludeId);
}

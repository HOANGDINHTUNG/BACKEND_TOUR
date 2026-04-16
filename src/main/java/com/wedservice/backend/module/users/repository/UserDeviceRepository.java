package com.wedservice.backend.module.users.repository;

import com.wedservice.backend.module.users.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findByUserIdAndIsActiveTrueOrderByLastSeenAtDescIdDesc(UUID userId);

    Optional<UserDevice> findByIdAndUserId(Long id, UUID userId);

    Optional<UserDevice> findFirstByUserIdAndPushToken(UUID userId, String pushToken);
}

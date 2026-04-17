package com.wedservice.backend.module.support.repository;

import com.wedservice.backend.module.support.entity.SupportSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportSessionRepository extends JpaRepository<SupportSession, Long>, JpaSpecificationExecutor<SupportSession> {

    List<SupportSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    Optional<SupportSession> findByIdAndUserId(Long id, UUID userId);

    List<SupportSession> findAllByOrderByUpdatedAtDesc();
}

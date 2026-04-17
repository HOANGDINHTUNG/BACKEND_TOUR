package com.wedservice.backend.module.support.repository;

import com.wedservice.backend.module.support.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}

package com.wedservice.backend.module.schedulechat.repository;

import com.wedservice.backend.module.schedulechat.entity.ScheduleChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleChatMessageRepository extends JpaRepository<ScheduleChatMessage, Long> {

    List<ScheduleChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    Page<ScheduleChatMessage> findByRoomId(Long roomId, Pageable pageable);
}

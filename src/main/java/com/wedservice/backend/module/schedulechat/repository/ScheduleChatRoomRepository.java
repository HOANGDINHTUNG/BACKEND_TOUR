package com.wedservice.backend.module.schedulechat.repository;

import com.wedservice.backend.module.schedulechat.entity.ScheduleChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleChatRoomRepository extends JpaRepository<ScheduleChatRoom, Long> {

    Optional<ScheduleChatRoom> findByScheduleId(Long scheduleId);
}

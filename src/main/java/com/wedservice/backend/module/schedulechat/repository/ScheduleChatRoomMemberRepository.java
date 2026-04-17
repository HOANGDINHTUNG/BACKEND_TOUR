package com.wedservice.backend.module.schedulechat.repository;

import com.wedservice.backend.module.schedulechat.entity.ScheduleChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleChatRoomMemberRepository extends JpaRepository<ScheduleChatRoomMember, Long> {

    Optional<ScheduleChatRoomMember> findByRoomIdAndUserId(Long roomId, UUID userId);

    List<ScheduleChatRoomMember> findByRoomIdOrderByJoinedAtAsc(Long roomId);
}

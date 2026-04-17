package com.wedservice.backend.module.schedulechat.facade;

import com.wedservice.backend.module.schedulechat.dto.request.CreateScheduleChatMessageRequest;
import com.wedservice.backend.module.schedulechat.dto.request.UpsertScheduleChatRoomRequest;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatMessageResponse;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatRoomResponse;
import com.wedservice.backend.module.schedulechat.service.ScheduleChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminScheduleChatFacade {

    private final ScheduleChatService scheduleChatService;

    public ScheduleChatRoomResponse getRoom(Long scheduleId) {
        return scheduleChatService.getAdminRoom(scheduleId);
    }

    public ScheduleChatRoomResponse upsertRoom(Long scheduleId, UpsertScheduleChatRoomRequest request) {
        return scheduleChatService.upsertAdminRoom(scheduleId, request);
    }

    public Page<ScheduleChatMessageResponse> getMessages(Long scheduleId, Pageable pageable) {
        return scheduleChatService.getAdminMessages(scheduleId, pageable);
    }

    public ScheduleChatMessageResponse sendMessage(Long scheduleId, CreateScheduleChatMessageRequest request) {
        return scheduleChatService.sendAdminMessage(scheduleId, request);
    }

    public void muteMember(Long scheduleId, UUID userId, boolean muted) {
        scheduleChatService.muteMember(scheduleId, userId, muted);
    }
}

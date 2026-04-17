package com.wedservice.backend.module.schedulechat.facade;

import com.wedservice.backend.module.schedulechat.dto.request.CreateScheduleChatMessageRequest;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatMessageResponse;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatRoomResponse;
import com.wedservice.backend.module.schedulechat.service.ScheduleChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserScheduleChatFacade {

    private final ScheduleChatService scheduleChatService;

    public ScheduleChatRoomResponse getMyRoom(Long scheduleId) {
        return scheduleChatService.getMyRoom(scheduleId);
    }

    public Page<ScheduleChatMessageResponse> getMyMessages(Long scheduleId, Pageable pageable) {
        return scheduleChatService.getMyMessages(scheduleId, pageable);
    }

    public ScheduleChatMessageResponse sendMyMessage(Long scheduleId, CreateScheduleChatMessageRequest request) {
        return scheduleChatService.sendMyMessage(scheduleId, request);
    }
}

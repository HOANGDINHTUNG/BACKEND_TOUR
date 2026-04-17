package com.wedservice.backend.module.schedulechat.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.schedulechat.dto.request.CreateScheduleChatMessageRequest;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatMessageResponse;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatRoomResponse;
import com.wedservice.backend.module.schedulechat.facade.UserScheduleChatFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserScheduleChatController {

    private final UserScheduleChatFacade userScheduleChatFacade;

    @GetMapping("/{scheduleId}/chat-room")
    public ApiResponse<ScheduleChatRoomResponse> getMyRoom(@PathVariable Long scheduleId) {
        return ApiResponse.<ScheduleChatRoomResponse>builder()
                .success(true)
                .message("Schedule chat room fetched successfully")
                .data(userScheduleChatFacade.getMyRoom(scheduleId))
                .build();
    }

    @GetMapping("/{scheduleId}/chat-room/messages")
    public ApiResponse<Page<ScheduleChatMessageResponse>> getMyMessages(
            @PathVariable Long scheduleId,
            Pageable pageable
    ) {
        return ApiResponse.<Page<ScheduleChatMessageResponse>>builder()
                .success(true)
                .message("Schedule chat messages fetched successfully")
                .data(userScheduleChatFacade.getMyMessages(scheduleId, pageable))
                .build();
    }

    @PostMapping("/{scheduleId}/chat-room/messages")
    public ApiResponse<ScheduleChatMessageResponse> sendMyMessage(
            @PathVariable Long scheduleId,
            @Valid @RequestBody CreateScheduleChatMessageRequest request
    ) {
        return ApiResponse.<ScheduleChatMessageResponse>builder()
                .success(true)
                .message("Schedule chat message sent successfully")
                .data(userScheduleChatFacade.sendMyMessage(scheduleId, request))
                .build();
    }
}

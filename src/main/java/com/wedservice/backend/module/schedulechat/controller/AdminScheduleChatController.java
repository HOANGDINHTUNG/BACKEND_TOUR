package com.wedservice.backend.module.schedulechat.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.schedulechat.dto.request.CreateScheduleChatMessageRequest;
import com.wedservice.backend.module.schedulechat.dto.request.UpsertScheduleChatRoomRequest;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatMessageResponse;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatRoomResponse;
import com.wedservice.backend.module.schedulechat.facade.AdminScheduleChatFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleChatController {

    private final AdminScheduleChatFacade adminScheduleChatFacade;

    @GetMapping("/{scheduleId}/chat-room")
    @PreAuthorize("hasAuthority('schedule.view')")
    public ApiResponse<ScheduleChatRoomResponse> getRoom(@PathVariable Long scheduleId) {
        return ApiResponse.<ScheduleChatRoomResponse>builder()
                .success(true)
                .message("Schedule chat room fetched successfully")
                .data(adminScheduleChatFacade.getRoom(scheduleId))
                .build();
    }

    @PutMapping("/{scheduleId}/chat-room")
    @PreAuthorize("hasAuthority('schedule.update')")
    public ApiResponse<ScheduleChatRoomResponse> upsertRoom(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpsertScheduleChatRoomRequest request
    ) {
        return ApiResponse.<ScheduleChatRoomResponse>builder()
                .success(true)
                .message("Schedule chat room updated successfully")
                .data(adminScheduleChatFacade.upsertRoom(scheduleId, request))
                .build();
    }

    @GetMapping("/{scheduleId}/chat-room/messages")
    @PreAuthorize("hasAuthority('schedule.view')")
    public ApiResponse<Page<ScheduleChatMessageResponse>> getMessages(
            @PathVariable Long scheduleId,
            Pageable pageable
    ) {
        return ApiResponse.<Page<ScheduleChatMessageResponse>>builder()
                .success(true)
                .message("Schedule chat messages fetched successfully")
                .data(adminScheduleChatFacade.getMessages(scheduleId, pageable))
                .build();
    }

    @PostMapping("/{scheduleId}/chat-room/messages")
    @PreAuthorize("hasAuthority('schedule.update')")
    public ApiResponse<ScheduleChatMessageResponse> sendMessage(
            @PathVariable Long scheduleId,
            @Valid @RequestBody CreateScheduleChatMessageRequest request
    ) {
        return ApiResponse.<ScheduleChatMessageResponse>builder()
                .success(true)
                .message("Schedule chat message sent successfully")
                .data(adminScheduleChatFacade.sendMessage(scheduleId, request))
                .build();
    }

    @PatchMapping("/{scheduleId}/chat-room/members/{userId}/mute")
    @PreAuthorize("hasAuthority('schedule.update')")
    public ApiResponse<Void> muteMember(
            @PathVariable Long scheduleId,
            @PathVariable UUID userId,
            @RequestParam boolean muted
    ) {
        adminScheduleChatFacade.muteMember(scheduleId, userId, muted);
        return ApiResponse.<Void>builder()
                .success(true)
                .message(muted ? "Member muted successfully" : "Member unmuted successfully")
                .build();
    }
}

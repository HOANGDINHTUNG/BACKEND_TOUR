package com.wedservice.backend.module.support.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.support.dto.request.AssignSupportSessionRequest;
import com.wedservice.backend.module.support.dto.request.CreateSupportMessageRequest;
import com.wedservice.backend.module.support.dto.request.UpdateSupportSessionStatusRequest;
import com.wedservice.backend.module.support.dto.response.SupportMessageResponse;
import com.wedservice.backend.module.support.dto.response.SupportSessionResponse;
import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import com.wedservice.backend.module.support.facade.AdminSupportFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/support/sessions")
@RequiredArgsConstructor
public class AdminSupportController {

    private final AdminSupportFacade adminSupportFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('support.view')")
    public ApiResponse<List<SupportSessionResponse>> getSupportSessions(
            @RequestParam(required = false) SupportSessionStatus status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID assignedStaffId
    ) {
        return ApiResponse.<List<SupportSessionResponse>>builder()
                .success(true)
                .message("Support sessions fetched successfully")
                .data(adminSupportFacade.getSupportSessions(status, userId, assignedStaffId))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('support.view')")
    public ApiResponse<SupportSessionResponse> getSupportSession(@PathVariable Long id) {
        return ApiResponse.<SupportSessionResponse>builder()
                .success(true)
                .message("Support session fetched successfully")
                .data(adminSupportFacade.getSupportSession(id))
                .build();
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('support.assign')")
    public ApiResponse<SupportSessionResponse> assignSupportSession(
            @PathVariable Long id,
            @Valid @RequestBody AssignSupportSessionRequest request
    ) {
        return ApiResponse.<SupportSessionResponse>builder()
                .success(true)
                .message("Support session assigned successfully")
                .data(adminSupportFacade.assignSupportSession(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('support.assign')")
    public ApiResponse<SupportSessionResponse> updateSupportSessionStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupportSessionStatusRequest request
    ) {
        return ApiResponse.<SupportSessionResponse>builder()
                .success(true)
                .message("Support session status updated successfully")
                .data(adminSupportFacade.updateSupportSessionStatus(id, request))
                .build();
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("hasAuthority('support.reply')")
    public ApiResponse<SupportMessageResponse> sendSupportReply(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupportMessageRequest request
    ) {
        return ApiResponse.<SupportMessageResponse>builder()
                .success(true)
                .message("Support reply sent successfully")
                .data(adminSupportFacade.sendSupportReply(id, request))
                .build();
    }
}

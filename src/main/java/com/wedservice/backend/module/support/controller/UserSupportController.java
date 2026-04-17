package com.wedservice.backend.module.support.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.support.dto.request.CreateSupportMessageRequest;
import com.wedservice.backend.module.support.dto.request.CreateSupportSessionRequest;
import com.wedservice.backend.module.support.dto.request.RateSupportSessionRequest;
import com.wedservice.backend.module.support.dto.response.SupportMessageResponse;
import com.wedservice.backend.module.support.dto.response.SupportSessionResponse;
import com.wedservice.backend.module.support.facade.UserSupportFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me/support/sessions")
@RequiredArgsConstructor
public class UserSupportController {

    private final UserSupportFacade userSupportFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('support.reply')")
    public ApiResponse<SupportSessionResponse> createMySupportSession(@Valid @RequestBody CreateSupportSessionRequest request) {
        return ApiResponse.<SupportSessionResponse>builder()
                .success(true)
                .message("Support session created successfully")
                .data(userSupportFacade.createMySupportSession(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('support.view')")
    public ApiResponse<List<SupportSessionResponse>> getMySupportSessions() {
        return ApiResponse.<List<SupportSessionResponse>>builder()
                .success(true)
                .message("Support sessions fetched successfully")
                .data(userSupportFacade.getMySupportSessions())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('support.view')")
    public ApiResponse<SupportSessionResponse> getMySupportSession(@PathVariable Long id) {
        return ApiResponse.<SupportSessionResponse>builder()
                .success(true)
                .message("Support session fetched successfully")
                .data(userSupportFacade.getMySupportSession(id))
                .build();
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("hasAuthority('support.reply')")
    public ApiResponse<SupportMessageResponse> sendMySupportMessage(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupportMessageRequest request
    ) {
        return ApiResponse.<SupportMessageResponse>builder()
                .success(true)
                .message("Support message sent successfully")
                .data(userSupportFacade.sendMySupportMessage(id, request))
                .build();
    }

    @PatchMapping("/{id}/rate")
    @PreAuthorize("hasAuthority('support.view')")
    public ApiResponse<SupportSessionResponse> rateMySupportSession(
            @PathVariable Long id,
            @Valid @RequestBody RateSupportSessionRequest request
    ) {
        return ApiResponse.<SupportSessionResponse>builder()
                .success(true)
                .message("Support session rated successfully")
                .data(userSupportFacade.rateMySupportSession(id, request))
                .build();
    }
}

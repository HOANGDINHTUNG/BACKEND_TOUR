package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.loyalty.dto.request.CreateUserCheckinRequest;
import com.wedservice.backend.module.loyalty.dto.response.UserCheckinResponse;
import com.wedservice.backend.module.loyalty.facade.AdminUserCheckinFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/checkins")
@RequiredArgsConstructor
public class AdminUserCheckinController {

    private final AdminUserCheckinFacade adminUserCheckinFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('booking.checkin')")
    public ApiResponse<UserCheckinResponse> createCheckin(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateUserCheckinRequest request
    ) {
        return ApiResponse.<UserCheckinResponse>builder()
                .success(true)
                .message("User checkin created successfully")
                .data(adminUserCheckinFacade.createCheckin(userId, request))
                .build();
    }
}

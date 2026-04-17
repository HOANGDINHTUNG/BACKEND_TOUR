package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.loyalty.dto.request.AdminBadgeRequest;
import com.wedservice.backend.module.loyalty.dto.request.UpdateBadgeStatusRequest;
import com.wedservice.backend.module.loyalty.dto.response.BadgeResponse;
import com.wedservice.backend.module.loyalty.dto.response.PassportBadgeResponse;
import com.wedservice.backend.module.loyalty.facade.AdminBadgeFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
public class AdminBadgeController {

    private final AdminBadgeFacade adminBadgeFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<List<BadgeResponse>> getBadges() {
        return ApiResponse.<List<BadgeResponse>>builder()
                .success(true)
                .message("Badges fetched successfully")
                .data(adminBadgeFacade.getBadges())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<BadgeResponse> getBadge(@PathVariable Long id) {
        return ApiResponse.<BadgeResponse>builder()
                .success(true)
                .message("Badge fetched successfully")
                .data(adminBadgeFacade.getBadge(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user.update')")
    public ApiResponse<BadgeResponse> createBadge(@Valid @RequestBody AdminBadgeRequest request) {
        return ApiResponse.<BadgeResponse>builder()
                .success(true)
                .message("Badge created successfully")
                .data(adminBadgeFacade.createBadge(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.update')")
    public ApiResponse<BadgeResponse> updateBadge(@PathVariable Long id, @Valid @RequestBody AdminBadgeRequest request) {
        return ApiResponse.<BadgeResponse>builder()
                .success(true)
                .message("Badge updated successfully")
                .data(adminBadgeFacade.updateBadge(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user.update')")
    public ApiResponse<BadgeResponse> updateBadgeStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBadgeStatusRequest request
    ) {
        return ApiResponse.<BadgeResponse>builder()
                .success(true)
                .message("Badge status updated successfully")
                .data(adminBadgeFacade.updateBadgeStatus(id, request))
                .build();
    }

    @PostMapping("/{badgeId}/grant/users/{userId}")
    @PreAuthorize("hasAuthority('user.update')")
    public ApiResponse<PassportBadgeResponse> grantBadge(
            @PathVariable Long badgeId,
            @PathVariable UUID userId
    ) {
        return ApiResponse.<PassportBadgeResponse>builder()
                .success(true)
                .message("Badge granted successfully")
                .data(adminBadgeFacade.grantBadge(userId, badgeId))
                .build();
    }
}

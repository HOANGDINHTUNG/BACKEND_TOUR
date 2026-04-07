package com.wedservice.backend.module.destinations.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.FollowDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationFollowResponse;
import com.wedservice.backend.module.destinations.service.DestinationFollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DestinationFollowController {

    private final DestinationFollowService followService;

    @PostMapping("/{uuid}/follow")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DestinationFollowResponse> followDestination(
            @PathVariable UUID uuid,
            @Valid @RequestBody(required = false) FollowDestinationRequest request
    ) {
        if (request == null) {
            request = new FollowDestinationRequest();
        }
        return ApiResponse.success(followService.followDestination(uuid, request), "Follow destination successfully");
    }

    @DeleteMapping("/{uuid}/follow")
    public ApiResponse<Void> unfollowDestination(@PathVariable UUID uuid) {
        followService.unfollowDestination(uuid);
        return ApiResponse.success(null, "Unfollow destination successfully");
    }

    @PutMapping("/{uuid}/follow/settings")
    public ApiResponse<DestinationFollowResponse> updateFollowSettings(
            @PathVariable UUID uuid,
            @Valid @RequestBody FollowDestinationRequest request
    ) {
        return ApiResponse.success(followService.updateFollowSettings(uuid, request), "Update follow settings successfully");
    }

    @GetMapping("/me/follows")
    public ApiResponse<PageResponse<DestinationFollowResponse>> getMyFollows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(followService.getMyFollows(page, size));
    }
}

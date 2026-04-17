package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.loyalty.dto.response.UserMissionResponse;
import com.wedservice.backend.module.loyalty.facade.UserMissionFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/me/missions")
@RequiredArgsConstructor
public class UserMissionController {

    private final UserMissionFacade userMissionFacade;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<UserMissionResponse>> getMyMissions() {
        return ApiResponse.<List<UserMissionResponse>>builder()
                .success(true)
                .message("User missions fetched successfully")
                .data(userMissionFacade.getMyMissions())
                .build();
    }

    @PostMapping("/{id}/claim")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserMissionResponse> claimReward(@PathVariable Long id) {
        return ApiResponse.<UserMissionResponse>builder()
                .success(true)
                .message("Reward claimed successfully")
                .data(userMissionFacade.claimReward(id))
                .build();
    }
}

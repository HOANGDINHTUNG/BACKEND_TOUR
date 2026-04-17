package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.loyalty.dto.request.CreateMissionRequest;
import com.wedservice.backend.module.loyalty.dto.response.MissionResponse;
import com.wedservice.backend.module.loyalty.facade.AdminMissionFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/missions")
@RequiredArgsConstructor
public class AdminMissionController {

    private final AdminMissionFacade adminMissionFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('loyalty.view')")
    public ApiResponse<List<MissionResponse>> getAllMissions() {
        return ApiResponse.<List<MissionResponse>>builder()
                .success(true)
                .message("Missions fetched successfully")
                .data(adminMissionFacade.getAllMissions())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('loyalty.view')")
    public ApiResponse<MissionResponse> getMissionById(@PathVariable Long id) {
        return ApiResponse.<MissionResponse>builder()
                .success(true)
                .message("Mission fetched successfully")
                .data(adminMissionFacade.getMissionById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('loyalty.update')")
    public ApiResponse<MissionResponse> createMission(@Valid @RequestBody CreateMissionRequest request) {
        return ApiResponse.<MissionResponse>builder()
                .success(true)
                .message("Mission created successfully")
                .data(adminMissionFacade.createMission(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('loyalty.update')")
    public ApiResponse<MissionResponse> updateMission(@PathVariable Long id, @Valid @RequestBody CreateMissionRequest request) {
        return ApiResponse.<MissionResponse>builder()
                .success(true)
                .message("Mission updated successfully")
                .data(adminMissionFacade.updateMission(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('loyalty.update')")
    public ApiResponse<MissionResponse> toggleMissionStatus(@PathVariable Long id) {
        return ApiResponse.<MissionResponse>builder()
                .success(true)
                .message("Mission status toggled successfully")
                .data(adminMissionFacade.toggleMissionStatus(id))
                .build();
    }
}

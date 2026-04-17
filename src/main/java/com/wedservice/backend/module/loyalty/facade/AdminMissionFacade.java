package com.wedservice.backend.module.loyalty.facade;

import com.wedservice.backend.module.loyalty.dto.request.CreateMissionRequest;
import com.wedservice.backend.module.loyalty.dto.response.MissionResponse;
import com.wedservice.backend.module.loyalty.service.AdminMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminMissionFacade {

    private final AdminMissionService adminMissionService;

    public List<MissionResponse> getAllMissions() {
        return adminMissionService.getAllMissions();
    }

    public MissionResponse getMissionById(Long id) {
        return adminMissionService.getMissionById(id);
    }

    public MissionResponse createMission(CreateMissionRequest request) {
        return adminMissionService.createMission(request);
    }

    public MissionResponse updateMission(Long id, CreateMissionRequest request) {
        return adminMissionService.updateMission(id, request);
    }

    public MissionResponse toggleMissionStatus(Long id) {
        return adminMissionService.toggleMissionStatus(id);
    }
}

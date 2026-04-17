package com.wedservice.backend.module.loyalty.facade;

import com.wedservice.backend.module.loyalty.dto.response.UserMissionResponse;
import com.wedservice.backend.module.loyalty.service.UserMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMissionFacade {

    private final UserMissionService userMissionService;

    public List<UserMissionResponse> getMyMissions() {
        return userMissionService.getMyMissions();
    }

    public UserMissionResponse claimReward(Long userMissionId) {
        return userMissionService.claimMissionReward(userMissionId);
    }
}

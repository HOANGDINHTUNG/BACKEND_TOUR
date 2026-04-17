package com.wedservice.backend.module.loyalty.mapper;

import com.wedservice.backend.module.loyalty.dto.response.MissionResponse;
import com.wedservice.backend.module.loyalty.dto.response.UserMissionResponse;
import com.wedservice.backend.module.loyalty.entity.MissionDefinition;
import com.wedservice.backend.module.loyalty.entity.UserMission;
import org.springframework.stereotype.Component;

@Component
public class MissionMapper {

    public MissionResponse toMissionResponse(MissionDefinition mission) {
        if (mission == null) return null;
        return MissionResponse.builder()
                .id(mission.getId())
                .code(mission.getCode())
                .name(mission.getName())
                .description(mission.getDescription())
                .ruleJson(mission.getRuleJson())
                .rewardType(mission.getRewardType())
                .rewardValue(mission.getRewardValue())
                .rewardRefId(mission.getRewardRefId())
                .startAt(mission.getStartAt())
                .endAt(mission.getEndAt())
                .isActive(mission.isActive())
                .createdAt(mission.getCreatedAt())
                .build();
    }

    public UserMissionResponse toUserMissionResponse(UserMission userMission) {
        if (userMission == null) return null;
        return UserMissionResponse.builder()
                .id(userMission.getId())
                .mission(toMissionResponse(userMission.getMission()))
                .progress(userMission.getProgress())
                .goal(userMission.getGoal())
                .status(userMission.getStatus())
                .completedAt(userMission.getCompletedAt())
                .claimedAt(userMission.getClaimedAt())
                .build();
    }
}

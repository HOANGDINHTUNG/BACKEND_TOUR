package com.wedservice.backend.module.loyalty.service;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.loyalty.dto.request.CreateMissionRequest;
import com.wedservice.backend.module.loyalty.dto.response.MissionResponse;
import com.wedservice.backend.module.loyalty.entity.MissionDefinition;
import com.wedservice.backend.module.loyalty.repository.MissionDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMissionService {

    private final MissionDefinitionRepository missionDefinitionRepository;

    @Transactional(readOnly = true)
    public List<MissionResponse> getAllMissions() {
        return missionDefinitionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MissionResponse getMissionById(Long id) {
        return missionDefinitionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));
    }

    @Transactional
    public MissionResponse createMission(CreateMissionRequest request) {
        MissionDefinition mission = MissionDefinition.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .ruleJson(request.getRuleJson())
                .rewardType(request.getRewardType())
                .rewardValue(request.getRewardValue())
                .rewardRefId(request.getRewardRefId())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .isActive(true)
                .build();

        return toResponse(missionDefinitionRepository.save(mission));
    }

    @Transactional
    public MissionResponse updateMission(Long id, CreateMissionRequest request) {
        MissionDefinition mission = missionDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        mission.setName(request.getName());
        mission.setDescription(request.getDescription());
        mission.setRuleJson(request.getRuleJson());
        mission.setRewardType(request.getRewardType());
        mission.setRewardValue(request.getRewardValue());
        mission.setRewardRefId(request.getRewardRefId());
        mission.setStartAt(request.getStartAt());
        mission.setEndAt(request.getEndAt());

        return toResponse(missionDefinitionRepository.save(mission));
    }

    @Transactional
    public MissionResponse toggleMissionStatus(Long id) {
        MissionDefinition mission = missionDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        mission.setActive(!mission.isActive());
        return toResponse(missionDefinitionRepository.save(mission));
    }

    private MissionResponse toResponse(MissionDefinition mission) {
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
}

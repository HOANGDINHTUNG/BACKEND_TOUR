package com.wedservice.backend.module.loyalty.service;

import com.wedservice.backend.module.loyalty.entity.MissionDefinition;
import com.wedservice.backend.module.loyalty.entity.UserMission;
import com.wedservice.backend.module.loyalty.entity.UserMissionStatus;
import com.wedservice.backend.module.loyalty.repository.MissionDefinitionRepository;
import com.wedservice.backend.module.loyalty.repository.UserMissionRepository;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import com.wedservice.backend.module.notifications.service.InternalNotificationService;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionTrackerService {

    private final MissionDefinitionRepository missionDefinitionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;
    private final InternalNotificationService internalNotificationService;

    @Transactional
    public void incrementProgress(UUID userId, String missionCode, BigDecimal amount) {
        missionDefinitionRepository.findByCode(missionCode.toUpperCase())
                .filter(this::isMissionActive)
                .ifPresent(mission -> {
                    UserMission userMission = userMissionRepository.findByUserIdAndMissionId(userId, mission.getId())
                            .orElseGet(() -> createInitialUserMission(userId, mission));

                    if (userMission.getStatus() == UserMissionStatus.IN_PROGRESS) {
                        BigDecimal newProgress = userMission.getProgress().add(amount);
                        userMission.setProgress(newProgress);

                        if (newProgress.compareTo(userMission.getGoal()) >= 0) {
                            userMission.setStatus(UserMissionStatus.COMPLETED);
                            userMission.setCompletedAt(LocalDateTime.now());
                            sendCompletionNotification(userId, mission);
                        }
                        userMissionRepository.save(userMission);
                        log.info("Mission {} progress updated for user {}: {}/{}", 
                                mission.getCode(), userId, userMission.getProgress(), userMission.getGoal());
                    }
                });
    }

    private boolean isMissionActive(MissionDefinition mission) {
        if (!mission.isActive()) return false;
        LocalDateTime now = LocalDateTime.now();
        if (mission.getStartAt() != null && now.isBefore(mission.getStartAt())) return false;
        if (mission.getEndAt() != null && now.isAfter(mission.getEndAt())) return false;
        return true;
    }

    private UserMission createInitialUserMission(UUID userId, MissionDefinition mission) {
        User user = userRepository.getReferenceById(userId);
        return UserMission.builder()
                .user(user)
                .mission(mission)
                .progress(BigDecimal.ZERO)
                .goal(BigDecimal.valueOf(1)) // Default goal, could be fetched from ruleJson
                .status(UserMissionStatus.IN_PROGRESS)
                .build();
    }

    private void sendCompletionNotification(UUID userId, MissionDefinition mission) {
        internalNotificationService.sendInAppNotification(
                userId,
                NotificationType.SYSTEM,
                "Nhiệm vụ hoàn tất!",
                "Chúc mừng! Bạn đã hoàn thành nhiệm vụ: " + mission.getName() + ". Hãy vào nhận thưởng ngay!",
                "MISSION",
                mission.getId(),
                null
        );
    }
}

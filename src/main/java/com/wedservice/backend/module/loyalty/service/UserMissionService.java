package com.wedservice.backend.module.loyalty.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.loyalty.dto.response.UserMissionResponse;
import com.wedservice.backend.module.loyalty.entity.MissionDefinition;
import com.wedservice.backend.module.loyalty.entity.MissionRewardType;
import com.wedservice.backend.module.loyalty.entity.UserMission;
import com.wedservice.backend.module.loyalty.entity.UserMissionStatus;
import com.wedservice.backend.module.loyalty.mapper.MissionMapper;
import com.wedservice.backend.module.loyalty.repository.UserMissionRepository;
import com.wedservice.backend.module.promotions.entity.VoucherUserClaim;
import com.wedservice.backend.module.promotions.repository.VoucherUserClaimRepository;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMissionService {

    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;
    private final VoucherUserClaimRepository voucherUserClaimRepository;
    private final MissionMapper missionMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<UserMissionResponse> getMyMissions() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        return userMissionRepository.findByUserId(userId).stream()
                .map(missionMapper::toUserMissionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserMissionResponse claimMissionReward(Long userMissionId) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        UserMission userMission = userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new ResourceNotFoundException("User mission not found"));

        if (!userMission.getUser().getId().equals(userId)) {
            throw new BadRequestException("This mission does not belong to you");
        }

        if (userMission.getStatus() != UserMissionStatus.COMPLETED) {
            throw new BadRequestException("Mission is not completed or already claimed");
        }

        MissionDefinition mission = userMission.getMission();
        processReward(userId, mission);

        userMission.setStatus(UserMissionStatus.CLAIMED);
        userMission.setClaimedAt(LocalDateTime.now());

        return missionMapper.toUserMissionResponse(userMissionRepository.save(userMission));
    }

    private void processReward(UUID userId, MissionDefinition mission) {
        if (mission.getRewardType() == MissionRewardType.POINTS) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setLoyaltyPoints(user.getLoyaltyPoints() + mission.getRewardValue().intValue());
            userRepository.save(user);
        } else if (mission.getRewardType() == MissionRewardType.VOUCHER) {
            if (mission.getRewardRefId() == null) {
                throw new BadRequestException("Voucher reward missing reference id");
            }
            // Create voucher claim for the user
            VoucherUserClaim claim = VoucherUserClaim.builder()
                    .voucherId(mission.getRewardRefId())
                    .userId(userId)
                    .usedCount(0)
                    .build();
            voucherUserClaimRepository.save(claim);
        }
        // GIFT type could trigger a notification or manual fulfillment
    }
}

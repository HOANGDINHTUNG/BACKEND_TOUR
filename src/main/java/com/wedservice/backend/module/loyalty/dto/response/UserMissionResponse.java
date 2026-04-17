package com.wedservice.backend.module.loyalty.dto.response;

import com.wedservice.backend.module.loyalty.entity.UserMissionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMissionResponse {
    private Long id;
    private MissionResponse mission;
    private BigDecimal progress;
    private BigDecimal goal;
    private UserMissionStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime claimedAt;
}

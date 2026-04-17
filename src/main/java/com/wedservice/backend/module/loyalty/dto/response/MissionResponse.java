package com.wedservice.backend.module.loyalty.dto.response;

import com.wedservice.backend.module.loyalty.entity.MissionRewardType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String ruleJson;
    private MissionRewardType rewardType;
    private BigDecimal rewardValue;
    private Long rewardRefId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean isActive;
    private LocalDateTime createdAt;
}

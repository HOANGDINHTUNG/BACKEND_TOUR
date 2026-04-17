package com.wedservice.backend.module.loyalty.dto.request;

import com.wedservice.backend.module.loyalty.entity.MissionRewardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMissionRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 200)
    private String name;

    private String description;

    private String ruleJson;

    @NotNull
    private MissionRewardType rewardType;

    @NotNull
    private BigDecimal rewardValue;

    private Long rewardRefId;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}

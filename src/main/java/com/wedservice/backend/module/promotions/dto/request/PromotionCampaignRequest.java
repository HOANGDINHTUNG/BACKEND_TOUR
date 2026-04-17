package com.wedservice.backend.module.promotions.dto.request;

import com.wedservice.backend.module.users.entity.MemberLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCampaignRequest {

    @NotBlank(message = "code is required")
    @Size(max = 40, message = "code must not exceed 40 characters")
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 200, message = "name must not exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "startAt is required")
    private LocalDateTime startAt;

    @NotNull(message = "endAt is required")
    private LocalDateTime endAt;

    private MemberLevel targetMemberLevel;

    private JsonNode conditionsJson;

    private JsonNode rewardJson;

    private Boolean isActive;
}

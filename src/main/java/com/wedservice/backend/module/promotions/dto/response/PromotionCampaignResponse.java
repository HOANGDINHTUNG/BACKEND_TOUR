package com.wedservice.backend.module.promotions.dto.response;

import com.wedservice.backend.module.users.entity.MemberLevel;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Data
@Builder
public class PromotionCampaignResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private MemberLevel targetMemberLevel;
    private JsonNode conditionsJson;
    private JsonNode rewardJson;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

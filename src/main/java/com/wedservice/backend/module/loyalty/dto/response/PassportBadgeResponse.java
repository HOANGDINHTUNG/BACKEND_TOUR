package com.wedservice.backend.module.loyalty.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassportBadgeResponse {

    private Long passportBadgeId;
    private Long badgeId;
    private String badgeCode;
    private String badgeName;
    private String badgeDescription;
    private String iconUrl;
    private String conditionJson;
    private Boolean isActive;
    private LocalDateTime unlockedAt;
}

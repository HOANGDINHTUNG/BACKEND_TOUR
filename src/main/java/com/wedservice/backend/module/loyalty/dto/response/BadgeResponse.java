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
public class BadgeResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String iconUrl;
    private String conditionJson;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

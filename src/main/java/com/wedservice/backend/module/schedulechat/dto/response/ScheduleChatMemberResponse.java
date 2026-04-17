package com.wedservice.backend.module.schedulechat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleChatMemberResponse {

    private Long id;
    private UUID userId;
    private String fullName;
    private String displayName;
    private Boolean muted;
    private LocalDateTime joinedAt;
}

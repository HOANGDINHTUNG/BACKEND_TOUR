package com.wedservice.backend.module.support.dto.response;

import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportSessionResponse {

    private Long id;
    private String sessionCode;
    private UUID userId;
    private UUID assignedStaffId;
    private SupportSessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer rating;
    private String feedback;
    private Integer messageCount;
    private List<SupportMessageResponse> messages;
}

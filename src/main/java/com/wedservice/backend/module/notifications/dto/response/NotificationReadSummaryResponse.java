package com.wedservice.backend.module.notifications.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationReadSummaryResponse {
    private Integer updatedCount;
}

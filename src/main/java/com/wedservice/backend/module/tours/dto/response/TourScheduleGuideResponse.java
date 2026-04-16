package com.wedservice.backend.module.tours.dto.response;

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
public class TourScheduleGuideResponse {

    private Long id;
    private Long guideId;
    private String guideCode;
    private String guideFullName;
    private String guidePhone;
    private String guideEmail;
    private String guideStatus;
    private Boolean isLocalGuide;
    private String guideRole;
    private LocalDateTime assignedAt;
}

package com.wedservice.backend.module.loyalty.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCheckinResponse {

    private Long id;
    private UUID userId;
    private Long bookingId;
    private String bookingCode;
    private Long destinationId;
    private UUID destinationUuid;
    private String destinationName;
    private String destinationSlug;
    private BigDecimal checkinLatitude;
    private BigDecimal checkinLongitude;
    private String note;
    private LocalDateTime createdAt;
}

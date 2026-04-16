package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourScheduleRequest {

    private String scheduleCode;

    @NotNull
    private LocalDateTime departureAt;

    @NotNull
    private LocalDateTime returnAt;

    private LocalDateTime bookingOpenAt;

    private LocalDateTime bookingCloseAt;

    private LocalDateTime meetingAt;

    private String meetingPointName;

    private String meetingAddress;

    private BigDecimal meetingLatitude;

    private BigDecimal meetingLongitude;

    @NotNull
    @Min(1)
    private Integer capacityTotal;

    @Min(1)
    @Builder.Default
    private Integer minGuestsToOperate = 1;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal adultPrice;

    @DecimalMin(value = "0.0")
    @Builder.Default
    private BigDecimal childPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    @Builder.Default
    private BigDecimal infantPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    @Builder.Default
    private BigDecimal seniorPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    @Builder.Default
    private BigDecimal singleRoomSurcharge = BigDecimal.ZERO;

    private String transportDetail;

    private String note;

    private String status;

    private List<@Valid TourSchedulePickupPointRequest> pickupPoints;

    private List<@Valid TourScheduleGuideRequest> guideAssignments;
}

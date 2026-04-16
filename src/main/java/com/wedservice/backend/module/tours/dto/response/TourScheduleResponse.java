package com.wedservice.backend.module.tours.dto.response;

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
public class TourScheduleResponse {

    private Long id;
    private String scheduleCode;
    private Long tourId;
    private LocalDateTime departureAt;
    private LocalDateTime returnAt;
    private LocalDateTime bookingOpenAt;
    private LocalDateTime bookingCloseAt;
    private LocalDateTime meetingAt;
    private String meetingPointName;
    private String meetingAddress;
    private BigDecimal meetingLatitude;
    private BigDecimal meetingLongitude;
    private Integer capacityTotal;
    private Integer bookedSeats;
    private Integer remainingSeats;
    private Integer minGuestsToOperate;
    private BigDecimal adultPrice;
    private BigDecimal childPrice;
    private BigDecimal infantPrice;
    private BigDecimal seniorPrice;
    private BigDecimal singleRoomSurcharge;
    private String transportDetail;
    private String note;
    private String status;
    private List<TourSchedulePickupPointResponse> pickupPoints;
    private List<TourScheduleGuideResponse> guideAssignments;
}

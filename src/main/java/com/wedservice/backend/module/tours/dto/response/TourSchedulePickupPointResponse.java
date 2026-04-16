package com.wedservice.backend.module.tours.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSchedulePickupPointResponse {

    private Long id;
    private String pointName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime pickupAt;
    private Integer sortOrder;
}

package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
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
public class TourSchedulePickupPointRequest {

    @NotBlank
    private String pointName;

    @NotBlank
    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDateTime pickupAt;

    @PositiveOrZero
    @Builder.Default
    private Integer sortOrder = 0;
}

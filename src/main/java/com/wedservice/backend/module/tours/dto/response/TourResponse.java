package com.wedservice.backend.module.tours.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourResponse {
    private Long id;
    private String code;
    private String name;
    private String slug;
    private Long destinationId;
    private BigDecimal basePrice;
    private String currency;
}

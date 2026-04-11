package com.wedservice.backend.module.tours.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSearchRequest {
    private Long destinationId;
    private String keyword;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
}

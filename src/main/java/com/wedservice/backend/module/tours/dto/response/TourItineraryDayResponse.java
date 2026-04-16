package com.wedservice.backend.module.tours.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourItineraryDayResponse {

    private Long id;
    private Integer dayNumber;
    private String title;
    private String description;
    private Long overnightDestinationId;
    private List<ItineraryItemResponse> items;
}

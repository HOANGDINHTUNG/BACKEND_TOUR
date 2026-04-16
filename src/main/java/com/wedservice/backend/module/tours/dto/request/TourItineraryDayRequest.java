package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class TourItineraryDayRequest {

    @NotNull
    @Min(1)
    private Integer dayNumber;

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;
    private Long overnightDestinationId;
    private List<@Valid ItineraryItemRequest> items;
}

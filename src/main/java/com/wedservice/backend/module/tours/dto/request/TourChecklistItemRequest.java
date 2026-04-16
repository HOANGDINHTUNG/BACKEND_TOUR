package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourChecklistItemRequest {

    @NotBlank
    @Size(max = 200)
    private String itemName;

    @Size(max = 80)
    private String itemGroup;

    @Builder.Default
    private Boolean isRequired = false;
}

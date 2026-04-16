package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.constraints.Min;
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
public class TourMediaRequest {

    @NotBlank
    @Size(max = 20)
    private String mediaType;

    @NotBlank
    private String mediaUrl;

    @Size(max = 255)
    private String altText;

    @Min(0)
    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isActive = true;
}

package com.wedservice.backend.module.destinations.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationTipRequest {

    @NotBlank(message = "Tip title is required")
    @Size(max = 200, message = "Tip title must not exceed 200 characters")
    private String tipTitle;

    @NotBlank(message = "Tip content is required")
    private String tipContent;

    private Integer sortOrder;
}

package com.wedservice.backend.module.destinations.dto.request;

import com.wedservice.backend.module.destinations.entity.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationMediaRequest {

    @NotNull(message = "Media type is required")
    private MediaType mediaType;

    @NotBlank(message = "Media URL is required")
    private String mediaUrl;

    private String altText;
    private Integer sortOrder;
    private Boolean isActive;
}

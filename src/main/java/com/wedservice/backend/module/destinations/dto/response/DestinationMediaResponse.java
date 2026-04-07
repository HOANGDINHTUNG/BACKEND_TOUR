package com.wedservice.backend.module.destinations.dto.response;

import com.wedservice.backend.module.destinations.entity.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationMediaResponse {
    private Long id;
    private MediaType mediaType;
    private String mediaUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isActive;
}

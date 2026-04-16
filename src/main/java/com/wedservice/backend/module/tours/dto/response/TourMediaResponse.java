package com.wedservice.backend.module.tours.dto.response;

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
public class TourMediaResponse {

    private Long id;
    private String mediaType;
    private String mediaUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isActive;
}

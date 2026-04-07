package com.wedservice.backend.module.destinations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationTipResponse {
    private Long id;
    private String tipTitle;
    private String tipContent;
    private Integer sortOrder;
}

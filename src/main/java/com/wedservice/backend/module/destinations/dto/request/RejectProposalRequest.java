package com.wedservice.backend.module.destinations.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectProposalRequest {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}

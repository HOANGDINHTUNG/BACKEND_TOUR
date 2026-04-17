package com.wedservice.backend.module.support.dto.request;

import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import jakarta.validation.constraints.NotNull;
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
public class UpdateSupportSessionStatusRequest {

    @NotNull
    private SupportSessionStatus status;
}

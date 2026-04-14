package com.wedservice.backend.module.destinations.dto.request;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposeDestinationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Province is required")
    @Size(max = 120, message = "Province must not exceed 120 characters")
    private String province;

    @Size(max = 120, message = "District must not exceed 120 characters")
    private String district;

    @Size(max = 120, message = "Region must not exceed 120 characters")
    private String region;

    @Size(max = 2, message = "Country code must be 2 characters")
    private String countryCode;

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String shortDescription;
    private String description;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer bestTimeFromMonth;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer bestTimeToMonth;

    private CrowdLevel crowdLevelDefault;

    @AssertTrue(message = "Best time range is invalid")
    public boolean isBestTimeRangeValid() {
        if (bestTimeFromMonth == null || bestTimeToMonth == null) {
            return true;
        }
        return bestTimeFromMonth <= bestTimeToMonth;
    }
}

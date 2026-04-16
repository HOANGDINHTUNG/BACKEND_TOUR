package com.wedservice.backend.module.users.dto.request;

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
public class UserAddressRequest {

    @NotBlank(message = "contactName is required")
    @Size(max = 150, message = "contactName must not exceed 150 characters")
    private String contactName;

    @NotBlank(message = "contactPhone is required")
    @Size(max = 20, message = "contactPhone must not exceed 20 characters")
    private String contactPhone;

    @Size(max = 100, message = "province must not exceed 100 characters")
    private String province;

    @Size(max = 100, message = "district must not exceed 100 characters")
    private String district;

    @Size(max = 100, message = "ward must not exceed 100 characters")
    private String ward;

    @NotBlank(message = "addressLine is required")
    private String addressLine;

    private Boolean isDefault;
}

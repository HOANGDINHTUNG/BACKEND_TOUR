package com.wedservice.backend.module.bookings.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {

    @NotNull
    private String userId;

    @NotNull
    private Long tourId;

    @NotNull
    private Long scheduleId;

    @NotBlank
    private String contactName;

    @NotBlank
    private String contactPhone;

    private String contactEmail;

    @Min(1)
    @Builder.Default
    private int adults = 1;

    @Builder.Default
    private int children = 0;

    @Builder.Default
    private int infants = 0;

    @Builder.Default
    private int seniors = 0;

    @Size(max = 50, message = "voucherCode must not exceed 50 characters")
    private String voucherCode;

    private Long comboId;

    private java.util.List<CreatePassengerRequest> passengers;
}

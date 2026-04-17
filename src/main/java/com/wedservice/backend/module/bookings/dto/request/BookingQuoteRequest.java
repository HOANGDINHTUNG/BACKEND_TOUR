package com.wedservice.backend.module.bookings.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class BookingQuoteRequest {

    @NotNull
    private Long tourId;

    @NotNull
    private Long scheduleId;

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
}

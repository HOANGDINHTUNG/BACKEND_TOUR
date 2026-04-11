package com.wedservice.backend.module.bookings.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private String status;
    private BigDecimal finalAmount;
}

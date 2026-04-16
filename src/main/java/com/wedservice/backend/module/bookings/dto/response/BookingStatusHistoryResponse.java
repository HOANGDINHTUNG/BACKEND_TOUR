package com.wedservice.backend.module.bookings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusHistoryResponse {

    private Long id;
    private String oldStatus;
    private String newStatus;
    private String changedBy;
    private String changeReason;
    private LocalDateTime createdAt;
}

package com.wedservice.backend.module.bookings.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePassengerRequest {

    @NotBlank
    private String fullName;

    private String passengerType; // adult, child, infant, senior

    private String gender;

    private String dateOfBirth;

    private String identityNo;

    private String passportNo;

    private String phone;

    private String email;
}

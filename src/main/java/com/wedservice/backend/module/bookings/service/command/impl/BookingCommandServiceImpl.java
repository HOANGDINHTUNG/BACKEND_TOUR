package com.wedservice.backend.module.bookings.service.command.impl;

import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.request.CreatePassengerRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPassenger;
import com.wedservice.backend.module.bookings.repository.BookingPassengerRepository;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.service.command.BookingCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingCommandServiceImpl implements BookingCommandService {

    private final BookingRepository bookingRepository;
    private final BookingPassengerRepository passengerRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Booking booking = Booking.builder()
                .bookingCode("BK" + System.currentTimeMillis())
                .userId(UUID.fromString(request.getUserId()))
                .tourId(request.getTourId())
                .scheduleId(request.getScheduleId())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .adults(request.getAdults())
                .children(request.getChildren())
                .infants(request.getInfants())
                .seniors(request.getSeniors())
                .subtotalAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .currency("VND")
                .build();

        booking = bookingRepository.save(booking);

        if (request.getPassengers() != null) {
            for (CreatePassengerRequest p : request.getPassengers()) {
                BookingPassenger bp = BookingPassenger.builder()
                        .bookingId(booking.getId())
                        .fullName(p.getFullName())
                        .passengerType(p.getPassengerType())
                        .gender(p.getGender())
                        .dateOfBirth(null)
                        .identityNo(p.getIdentityNo())
                        .passportNo(p.getPassportNo())
                        .phone(p.getPhone())
                        .email(p.getEmail())
                        .build();

                passengerRepository.save(bp);
            }
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus())
                .finalAmount(booking.getFinalAmount())
                .build();
    }
}

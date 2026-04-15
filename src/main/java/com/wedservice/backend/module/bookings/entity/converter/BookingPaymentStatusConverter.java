package com.wedservice.backend.module.bookings.entity.converter;

import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BookingPaymentStatusConverter implements AttributeConverter<BookingPaymentStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingPaymentStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public BookingPaymentStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BookingPaymentStatus.fromValue(dbData);
    }
}

package com.wedservice.backend.module.tours.entity.converter;

import com.wedservice.backend.module.tours.entity.TourStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TourStatusConverter implements AttributeConverter<TourStatus, String> {

    @Override
    public String convertToDatabaseColumn(TourStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TourStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TourStatus.fromValue(dbData);
    }
}

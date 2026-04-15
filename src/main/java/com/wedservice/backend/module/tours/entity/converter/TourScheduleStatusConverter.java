package com.wedservice.backend.module.tours.entity.converter;

import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TourScheduleStatusConverter implements AttributeConverter<TourScheduleStatus, String> {

    @Override
    public String convertToDatabaseColumn(TourScheduleStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TourScheduleStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TourScheduleStatus.fromValue(dbData);
    }
}

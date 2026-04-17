package com.wedservice.backend.module.weather.entity.converter;

import com.wedservice.backend.module.weather.entity.WeatherSeverity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class WeatherSeverityConverter implements AttributeConverter<WeatherSeverity, String> {

    @Override
    public String convertToDatabaseColumn(WeatherSeverity attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public WeatherSeverity convertToEntityAttribute(String dbData) {
        return dbData == null ? null : WeatherSeverity.fromValue(dbData);
    }
}

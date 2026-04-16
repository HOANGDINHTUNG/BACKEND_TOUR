package com.wedservice.backend.module.users.entity.converter;

import com.wedservice.backend.module.users.entity.TravelStyle;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TravelStyleConverter implements AttributeConverter<TravelStyle, String> {

    @Override
    public String convertToDatabaseColumn(TravelStyle attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TravelStyle convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TravelStyle.fromValue(dbData);
    }
}

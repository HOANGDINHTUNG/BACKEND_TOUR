package com.wedservice.backend.module.users.entity.converter;

import com.wedservice.backend.module.users.entity.PreferredTripMode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PreferredTripModeConverter implements AttributeConverter<PreferredTripMode, String> {

    @Override
    public String convertToDatabaseColumn(PreferredTripMode attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PreferredTripMode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PreferredTripMode.fromValue(dbData);
    }
}

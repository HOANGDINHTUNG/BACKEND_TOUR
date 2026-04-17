package com.wedservice.backend.module.support.entity.converter;

import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SupportSessionStatusConverter implements AttributeConverter<SupportSessionStatus, String> {

    @Override
    public String convertToDatabaseColumn(SupportSessionStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public SupportSessionStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SupportSessionStatus.fromValue(dbData);
    }
}

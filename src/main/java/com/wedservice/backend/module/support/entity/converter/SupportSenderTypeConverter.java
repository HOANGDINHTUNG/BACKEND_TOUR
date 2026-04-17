package com.wedservice.backend.module.support.entity.converter;

import com.wedservice.backend.module.support.entity.SupportSenderType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SupportSenderTypeConverter implements AttributeConverter<SupportSenderType, String> {

    @Override
    public String convertToDatabaseColumn(SupportSenderType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public SupportSenderType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SupportSenderType.fromValue(dbData);
    }
}

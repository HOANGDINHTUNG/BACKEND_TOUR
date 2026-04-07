package com.wedservice.backend.module.users.entity.converter;

import com.wedservice.backend.module.users.entity.Gender;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(Gender attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Gender convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Gender.fromValue(dbData);
    }
}

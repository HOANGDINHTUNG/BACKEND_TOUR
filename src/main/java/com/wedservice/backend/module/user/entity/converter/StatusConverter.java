package com.wedservice.backend.module.user.entity.converter;

import com.wedservice.backend.module.user.entity.Status;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatusConverter implements AttributeConverter<Status, String> {

    @Override
    public String convertToDatabaseColumn(Status attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Status convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Status.fromValue(dbData);
    }
}

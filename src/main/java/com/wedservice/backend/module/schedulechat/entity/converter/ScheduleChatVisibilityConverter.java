package com.wedservice.backend.module.schedulechat.entity.converter;

import com.wedservice.backend.module.schedulechat.entity.ScheduleChatVisibility;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ScheduleChatVisibilityConverter implements AttributeConverter<ScheduleChatVisibility, String> {

    @Override
    public String convertToDatabaseColumn(ScheduleChatVisibility attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ScheduleChatVisibility convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ScheduleChatVisibility.fromValue(dbData);
    }
}

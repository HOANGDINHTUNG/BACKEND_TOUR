package com.wedservice.backend.module.notifications.entity.converter;

import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NotificationChannelConverter implements AttributeConverter<NotificationChannel, String> {

    @Override
    public String convertToDatabaseColumn(NotificationChannel attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public NotificationChannel convertToEntityAttribute(String dbData) {
        return dbData == null ? null : NotificationChannel.fromValue(dbData);
    }
}

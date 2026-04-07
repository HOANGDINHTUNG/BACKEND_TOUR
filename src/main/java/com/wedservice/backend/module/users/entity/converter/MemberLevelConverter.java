package com.wedservice.backend.module.users.entity.converter;

import com.wedservice.backend.module.users.entity.MemberLevel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MemberLevelConverter implements AttributeConverter<MemberLevel, String> {

    @Override
    public String convertToDatabaseColumn(MemberLevel attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public MemberLevel convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MemberLevel.fromValue(dbData);
    }
}

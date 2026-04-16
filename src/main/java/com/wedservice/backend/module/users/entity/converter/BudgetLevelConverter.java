package com.wedservice.backend.module.users.entity.converter;

import com.wedservice.backend.module.users.entity.BudgetLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BudgetLevelConverter implements AttributeConverter<BudgetLevel, String> {

    @Override
    public String convertToDatabaseColumn(BudgetLevel attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public BudgetLevel convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BudgetLevel.fromValue(dbData);
    }
}

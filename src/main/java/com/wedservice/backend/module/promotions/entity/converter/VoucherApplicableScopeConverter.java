package com.wedservice.backend.module.promotions.entity.converter;

import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class VoucherApplicableScopeConverter implements AttributeConverter<VoucherApplicableScope, String> {

    @Override
    public String convertToDatabaseColumn(VoucherApplicableScope attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public VoucherApplicableScope convertToEntityAttribute(String dbData) {
        return dbData == null ? null : VoucherApplicableScope.fromValue(dbData);
    }
}

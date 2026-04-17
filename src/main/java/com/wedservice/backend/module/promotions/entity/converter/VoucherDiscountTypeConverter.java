package com.wedservice.backend.module.promotions.entity.converter;

import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class VoucherDiscountTypeConverter implements AttributeConverter<VoucherDiscountType, String> {

    @Override
    public String convertToDatabaseColumn(VoucherDiscountType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public VoucherDiscountType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : VoucherDiscountType.fromValue(dbData);
    }
}

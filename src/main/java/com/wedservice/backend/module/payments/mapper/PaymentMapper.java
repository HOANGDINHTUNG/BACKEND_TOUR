package com.wedservice.backend.module.payments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.wedservice.backend.common.mapper.BaseMapper;
import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.dto.response.PaymentResponse;
import com.wedservice.backend.module.payments.entity.Payment;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper extends BaseMapper<PaymentResponse, Payment> {

    @Override
    @Mapping(target = "id", source = "id")
    PaymentResponse toDto(Payment entity);

    @Mapping(target = "id", ignore = true)
    Payment toEntity(CreatePaymentRequest request);

    void updateEntity(@MappingTarget Payment payment, CreatePaymentRequest request);
}

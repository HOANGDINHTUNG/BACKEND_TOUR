package com.wedservice.backend.module.bookings.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.wedservice.backend.common.mapper.BaseMapper;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.entity.Booking;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingMapper extends BaseMapper<BookingResponse, Booking> {

    @Override
    @Mapping(target = "id", source = "id")
    BookingResponse toDto(Booking entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", expression = "java(java.util.UUID.fromString(request.getUserId()))")
    Booking toEntity(CreateBookingRequest request);

    void updateEntity(@MappingTarget Booking booking, CreateBookingRequest request);
}

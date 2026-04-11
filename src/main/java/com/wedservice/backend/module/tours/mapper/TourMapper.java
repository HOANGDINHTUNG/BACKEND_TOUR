package com.wedservice.backend.module.tours.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.wedservice.backend.common.mapper.BaseMapper;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.entity.Tour;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DataNormalizer.class})
public interface TourMapper extends BaseMapper<TourResponse, Tour> {

    @Override
    @Mapping(target = "id", source = "id")
    TourResponse toDto(Tour entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", expression = "java(DataNormalizer.normalize(request.getCode()))")
    @Mapping(target = "name", expression = "java(DataNormalizer.normalize(request.getName()))")
    @Mapping(target = "slug", expression = "java(DataNormalizer.normalize(request.getSlug()))")
    Tour toEntity(TourRequest request);

    @Mapping(target = "code", expression = "java(DataNormalizer.normalize(request.getCode()))")
    @Mapping(target = "name", expression = "java(DataNormalizer.normalize(request.getName()))")
    @Mapping(target = "slug", expression = "java(DataNormalizer.normalize(request.getSlug()))")
    void updateEntity(@MappingTarget Tour tour, TourRequest request);

    @AfterMapping
    default void sanitize(@MappingTarget Tour tour, TourRequest request) {
        if (request == null) return;
        if (request.getCurrency() == null) tour.setCurrency("VND");
    }
}

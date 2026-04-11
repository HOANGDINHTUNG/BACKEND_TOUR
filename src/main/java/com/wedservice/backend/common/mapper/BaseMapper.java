package com.wedservice.backend.common.mapper;

import java.util.List;

/**
 * Base mapper interface for MapStruct.
 */
public interface BaseMapper<D, E> {
    D toDto(E entity);
    E toEntity(D dto);
    List<D> toDtoList(List<E> entityList);
    List<E> toEntityList(List<D> dtoList);
}

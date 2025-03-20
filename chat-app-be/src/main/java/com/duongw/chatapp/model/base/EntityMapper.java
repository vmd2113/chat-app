package com.duongw.chatapp.model.base;


import org.mapstruct.*;

import java.util.List;

public interface EntityMapper <D, E> {


    @BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    E toEntity(D dto);


    @Named("toDto")
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    D toDto(E entity);


    @BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    List<E> toListEntity(List<D> DTOs);

    List<D> toListDto(List<E> entities);


    @Named("partialUpdate")
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void update(D dto, @MappingTarget E entity);

    @Named("partialUpdateDto")
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void updateDto(@MappingTarget D dto, E entity);


}

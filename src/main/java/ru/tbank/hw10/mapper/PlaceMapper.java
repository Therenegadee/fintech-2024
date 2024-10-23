package ru.tbank.hw10.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.tbank.hw10.dto.PlaceDto;
import ru.tbank.hw10.entity.Place;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlaceMapper {

    PlaceDto toDto(Place place);

    Place toEntity(PlaceDto dto);
}

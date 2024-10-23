package ru.tbank.hw10.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.tbank.hw10.dto.EventDto;
import ru.tbank.hw10.entity.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventsMapper {

    @Mapping(target = "placeId", source = "place.id")
    EventDto toDto(Event event);

    @Mapping(target = "place.id", source = "placeId")
    Event toEntity(EventDto dto);
}

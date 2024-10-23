package ru.tbank.hw10.v1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.hw10.dto.PlaceDto;
import ru.tbank.hw10.entity.Place;
import ru.tbank.hw10.exception.BadRequestException;
import ru.tbank.hw10.exception.NotFoundException;
import ru.tbank.hw10.mapper.PlaceMapper;
import ru.tbank.hw10.repository.PlaceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    public List<Place> findAllPlaces() {
        List<Place> places = placeRepository.findAll();
        if (places.isEmpty()) {
            throw new NotFoundException("В Базе Данных нет никакой информации о местах.");
        }
        return places;
    }

    public Place findPlaceById(Integer placeId) {
        return placeRepository.findPlaceById(placeId)
                .orElseThrow(() -> new NotFoundException("Информация о месте с идентификатором " + placeId + " не была найдена."));
    }

    public Place createPlace(PlaceDto placeDto) {
        Place place = placeMapper.toEntity(placeDto);
        place = placeRepository.save(place);
        return place;
    }

    public void deletePlace(Integer placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BadRequestException("Информации о месте с идентификатором " + placeId + " не существует!"));
        placeRepository.delete(place);
    }
}

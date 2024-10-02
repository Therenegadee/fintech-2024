package ru.tbank.hw5.service;

import ru.tbank.hw5.dto.PlaceCategory;

import java.util.List;
import java.util.Optional;

public interface PlaceCategoryService {
    List<PlaceCategory> getAllPlaceCategories();

    void saveAll(List<PlaceCategory> placeCategories);

    List<PlaceCategory> findAll();

    PlaceCategory findById(Integer id);

    PlaceCategory save(PlaceCategory placeCategory);

    PlaceCategory update(Integer id, PlaceCategory placeCategory);

    void delete(Integer id);
}

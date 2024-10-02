package ru.tbank.hw5.service;

import ru.tbank.hw5.dto.Location;

import java.util.List;
import java.util.Optional;

public interface LocationService {

    void saveAll(List<Location> locations);

    List<Location> findAll();

    Location findBySlug(String slug);

    Location save(Location location);

    Location update(String slug, Location location);

    void delete(String slug);
}

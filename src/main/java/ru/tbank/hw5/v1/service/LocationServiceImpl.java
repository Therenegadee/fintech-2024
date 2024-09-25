package ru.tbank.hw5.v1.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.hw5.annotation.ExecutionTimeObserved;
import ru.tbank.hw5.cache.LocationCache;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.exception.NotFoundException;
import ru.tbank.hw5.service.LocationService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.tbank.hw5.client.KudaGoApiClient.API_SERVICE_NAME;

@ExecutionTimeObserved
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final KudaGoApiClient kudaGoApiClient;
    private final LocationCache locationCache;

    @PostConstruct
    private List<Location> initCache() {
        log.debug("Начало наполнения кэша городов из сервиса {}.", API_SERVICE_NAME);
        List<Location> locations = kudaGoApiClient.getAllLocations();
        if (Objects.isNull(locations)) {
            String errorDetails = String.format("Полученный список городов из сервиса %s был null!", API_SERVICE_NAME);
            log.error(errorDetails);
            throw new IllegalStateException(errorDetails);
        }
        locationCache.saveAll(locations);
        log.debug("Кэш городов из сервиса {} был успешно наполнен. Список полученных городов содержал {} запись.",
                API_SERVICE_NAME, locations.size());
        return locations;
    }

    @Override
    public List<Location> getAllLocations() {
        log.info("Получение всех городов из кэша.");
        return locationCache.findAll();
    }

    @Override
    public void saveAll(List<Location> locations) {
        log.info("Начало сохранения списка городов в кэш. Размер входного списка: {}.", locations.size());
        locationCache.saveAll(locations);
        log.info("Список городов был успешно сохранен в кэш.");
    }

    @Override
    public List<Location> findAll() {
        log.info("Начало получения списка всех городов из кэша.");
        List<Location> locations = locationCache.findAll();
        log.info("Все города из кэша были успешно получены. Размер списка: {}.", locations.size());
        return locations;
    }

    @Override
    public Location findBySlug(String slug) {
        log.info("Начало получения города со slug \"{}\" из кэша.", slug);
        Location location = locationCache.findById(slug)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Город со slug %s не был найден!", slug);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
        log.info("Город со slug \"{}\" был успешно получен из кэша.", slug);
        return location;
    }

    @Override
    public Location save(Location location) {
        log.info("Начало сохранения в кэш города с именем: \"{}\".", location.getName());
        location = locationCache.save(location);
        log.info("Категория места с именем : \"{}\" была успешно сохранена в кэш со slug \"{}\".",
                location.getName(), location.getSlug());
        return location;
    }

    @Override
    public Location update(String slug, Location location) {
        log.info("Начало обновления в кэше города со slug : \"{}\".", slug);
        location = locationCache.update(slug, location);
        log.info("Город со slug \"{}\" была успешно обновлена в кэше.", slug);
        return location;
    }

    @Override
    public void delete(String slug) {
        log.info("Начало удаления из кэша города со slug \"{}\".", slug);
        locationCache.delete(slug);
        log.info("Город со slug \"{}\" был успешно удален из кэша.", slug);
    }
}
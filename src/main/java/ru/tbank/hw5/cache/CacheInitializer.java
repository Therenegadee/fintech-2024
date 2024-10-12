package ru.tbank.hw5.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.service.LocationService;
import ru.tbank.hw5.service.PlaceCategoryService;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static ru.tbank.hw5.client.KudaGoApiClient.API_SERVICE_NAME;

@MethodExecutionTimeTracked
@Component
@Slf4j
public class CacheInitializer {

    private final LocationService locationService;
    private final PlaceCategoryService placeCategoryService;
    private final KudaGoApiClient kudaGoApiClient;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ExecutorService fixedThreadPoolExecutorService;

    public CacheInitializer(LocationService locationService,
                            PlaceCategoryService placeCategoryService,
                            KudaGoApiClient kudaGoApiClient,
                            @Qualifier("kudaGoApiScheduledThreadPoolExecutorService") ScheduledExecutorService scheduledExecutorService,
                            @Qualifier("kudaGoApiFixedThreadPoolExecutorService") ExecutorService fixedThreadPoolExecutorService) {
        this.locationService = locationService;
        this.placeCategoryService = placeCategoryService;
        this.kudaGoApiClient = kudaGoApiClient;
        this.scheduledExecutorService = scheduledExecutorService;
        this.fixedThreadPoolExecutorService = fixedThreadPoolExecutorService;
    }

    @EventListener(ApplicationStartedEvent.class)
    protected void scheduleCacheDataUpdate() {
        scheduledExecutorService.schedule()
    }

    @EventListener(ApplicationStartedEvent.class)
    protected List<Location> fillLocationsCache() {
        log.debug("Начало наполнения кэша городов из сервиса {}.", API_SERVICE_NAME);
        List<Location> locations = kudaGoApiClient.getAllLocations();
        if (Objects.isNull(locations)) {
            String errorDetails = String.format("Полученный список городов из сервиса %s был null!", API_SERVICE_NAME);
            log.error(errorDetails);
            throw new IllegalStateException(errorDetails);
        }
        locationService.saveAll(locations);
        log.debug("Кэш городов из сервиса {} был успешно наполнен. Список полученных городов содержал {} запись.",
                API_SERVICE_NAME, locations.size());
        return locations;
    }

    @EventListener(ApplicationStartedEvent.class)
    protected List<PlaceCategory> fillPlaceCategoriesCache() {
        log.debug("Начало наполнения кэша категорий мест из сервиса {}.", API_SERVICE_NAME);
        List<PlaceCategory> placeCategories = kudaGoApiClient.getAllPlaceCategories();
        if (Objects.isNull(placeCategories)) {
            String errorDetails = String.format("Полученный список категорий мест из сервиса %s был null!", API_SERVICE_NAME);
            log.error(errorDetails);
            throw new IllegalStateException(errorDetails);
        }
        placeCategoryService.saveAll(placeCategories);
        log.debug("Кэш категорий мест из сервиса {} был успешно наполнен. Список полученных категорий мест содержал {} запись.",
                API_SERVICE_NAME, placeCategories.size());
        return placeCategories;
    }
}

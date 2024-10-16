package ru.tbank.hw5.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.service.LocationService;
import ru.tbank.hw5.service.PlaceCategoryService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.tbank.hw5.client.KudaGoApiClient.API_SERVICE_NAME;

@Component
@Slf4j
@MethodExecutionTimeTracked
public class CacheInitializer {

    private final LocationService locationService;
    private final PlaceCategoryService placeCategoryService;
    private final KudaGoApiClient kudaGoApiClient;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ExecutorService fixedThreadPoolExecutorService;

    @Value("${kudago-api.fill-cache.schedule}")
    private long scheduledCacheUpdateTimeSeconds;

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
        scheduledExecutorService.scheduleAtFixedRate(this::fillAllCaches, 0, scheduledCacheUpdateTimeSeconds, TimeUnit.SECONDS);
    }

    public void fillAllCaches() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Future<String>> responses = Arrays.stream(CacheType.values())
                .map(cacheType -> fixedThreadPoolExecutorService.submit(() -> fillCacheByType(cacheType)))
                .toList();
        responses.forEach(response -> {
                            try {
                                String responseMessage = response.get();
                                log.info("Результат наполнения кэша - \"{}\".", responseMessage);
                            } catch (Exception e) {
                                log.error("Произошла ошибка при наполнения одного из кэшей. Причина: {}." +
                                        "\nStackTrace: {}", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
                            }
                        }
                );
        stopWatch.stop();
        log.info("Наполнение всех кэшей заняло: {} ms.", stopWatch.getTotalTimeMillis());
    }

    private String fillCacheByType(CacheType cacheType) {
        return switch (cacheType) {
            case LOCATION -> fillLocationsCache();
            case PLACE_CATEGORY -> fillPlaceCategoriesCache();
        };
    }

    protected String fillLocationsCache() {
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
        return "Кэш городов был успешно наполнен.";
    }

    protected String fillPlaceCategoriesCache() {
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
        return "Кэш категорий мест был успешно наполнен.";
    }

    private enum CacheType {
        LOCATION,
        PLACE_CATEGORY
    }
}

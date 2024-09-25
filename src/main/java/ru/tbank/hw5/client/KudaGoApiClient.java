package ru.tbank.hw5.client;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.tbank.hw5.annotation.ExecutionTimeObserved;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.exception.IntegrationException;
import ru.tbank.hw5.exception.RestTemplateResponseErrorHandler;
import ru.tbank.hw5.interceptor.RestClientLoggingRequestInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@ExecutionTimeObserved
@Service
@Slf4j
@RequiredArgsConstructor
public class KudaGoApiClient {

    public static final String API_SERVICE_NAME = "KudaGo";
    private static final String KUDA_GO_API_BASE_URL = "https://kudago.com/public-api/v1.4";
    private static final String PLACES_CATEGORIES_PATH = "/place-categories";
    private static final String LOCATIONS_PATH = "/locations";
    private static final ReentrantLock LOCK = new ReentrantLock();

    private final RestTemplateResponseErrorHandler responseErrorHandler;
    private final RestClientLoggingRequestInterceptor requestLoggingInterceptor;

    private final RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;

    @PostConstruct
    private RestTemplate getRestTemplate() {
        LOCK.lock();
        if (Objects.isNull(restTemplate)) {
            restTemplate = restTemplateBuilder
                    .rootUri(KUDA_GO_API_BASE_URL)
                    .interceptors(requestLoggingInterceptor)
                    .errorHandler(responseErrorHandler)
                    .build();
        }
        LOCK.unlock();
        return restTemplate;
    }

    @Nullable
    public List<PlaceCategory> getAllPlaceCategories() {
        try {
            log.debug("Получение категорий мест из сервиса {}.", API_SERVICE_NAME);
            ResponseEntity<PlaceCategory[]> response = getRestTemplate()
                    .getForEntity(PLACES_CATEGORIES_PATH, PlaceCategory[].class);
            PlaceCategory[] placeCategories = response.getBody();
            if (Objects.isNull(placeCategories)) {
                log.error("Полученный список категорий мест из сервиса {} был null!", API_SERVICE_NAME);
                return null;
            }
            log.debug("Кол-во полученных категорий мест из сервиса {}: {}.", API_SERVICE_NAME, placeCategories.length);
            return Arrays.stream(placeCategories).toList();
        } catch (Exception e) {
            String errorMessage = String.format("В ходе получения категорий мест из сервиса %s произошла ошибка. Причина: %s.\nStackTrace: %s",
                    API_SERVICE_NAME, ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
            log.error(errorMessage);
            throw new IntegrationException(errorMessage);
        }
    }


    @Nullable
    public List<Location> getAllLocations() {
        try {
            log.debug("Получение городов из сервиса {}.", API_SERVICE_NAME);
            ResponseEntity<Location[]> response = getRestTemplate()
                    .getForEntity(LOCATIONS_PATH, Location[].class);
            Location[] locations = response.getBody();
            if (Objects.isNull(locations)) {
                log.error("Полученный список городов из сервиса {} был null!", API_SERVICE_NAME);
                return null;
            }
            log.debug("Кол-во полученных городов из сервиса {}: {}.", API_SERVICE_NAME, locations.length);
            return Arrays.stream(locations).toList();
        } catch (Exception e) {
            log.error("В ходе получения городов из сервиса {} произошла ошибка. Причина: {}.\nStackTrace: {}",
                    API_SERVICE_NAME, e.getMessage(), e.getStackTrace());
            return null;
        }
    }
}

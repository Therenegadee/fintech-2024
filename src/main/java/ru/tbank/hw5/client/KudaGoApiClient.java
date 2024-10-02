package ru.tbank.hw5.client;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.exception.IntegrationException;
import ru.tbank.hw5.exception.RestTemplateResponseErrorHandler;
import ru.tbank.hw5.interceptor.RestClientLoggingRequestInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@MethodExecutionTimeTracked
@Service
@Slf4j
@RequiredArgsConstructor
public class KudaGoApiClient {

    public static final String API_SERVICE_NAME = "KudaGo";
    @Value("${kudago-api.base-url}")
    private String baseUrl;
    @Value("${kudago-api.categories-path}")
    private String placeCategoriesPath;
    @Value("${kudago-api.locations-path}")
    private String locationsPath;

    private final RestTemplateResponseErrorHandler responseErrorHandler;
    private final RestClientLoggingRequestInterceptor requestLoggingInterceptor;

    private final RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;

    @PostConstruct
    private RestTemplate getRestTemplate() {
        restTemplate = restTemplateBuilder
                .rootUri(baseUrl)
                .interceptors(requestLoggingInterceptor)
                .errorHandler(responseErrorHandler)
                .build();
        return restTemplate;
    }

    @Nullable
    public List<PlaceCategory> getAllPlaceCategories() {
        try {
            log.debug("Получение категорий мест из сервиса {}.", API_SERVICE_NAME);
            ResponseEntity<PlaceCategory[]> response = restTemplate
                    .getForEntity(placeCategoriesPath, PlaceCategory[].class);
            PlaceCategory[] placeCategories = response.getBody();
            if (Objects.isNull(placeCategories)) {
                log.error("Полученный список категорий мест из сервиса {} был null!", API_SERVICE_NAME);
                return null;
            }
            log.debug("Кол-во полученных категорий мест из сервиса {}: {}.", API_SERVICE_NAME, placeCategories.length);
            return Arrays.stream(placeCategories).toList();
        } catch (RestClientException e) {
            String errorMessage = String.format("В ходе получения категорий мест из сервиса %s произошла ошибка. Причина: %s.\nStackTrace: %s",
                    API_SERVICE_NAME, ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
            log.error(errorMessage);
            throw new IntegrationException(errorMessage, ExceptionUtils.getRootCause(e));
        }
    }


    @Nullable
    public List<Location> getAllLocations() {
        try {
            log.debug("Получение городов из сервиса {}.", API_SERVICE_NAME);
            ResponseEntity<Location[]> response = restTemplate
                    .getForEntity(locationsPath, Location[].class);
            Location[] locations = response.getBody();
            if (Objects.isNull(locations)) {
                log.error("Полученный список городов из сервиса {} был null!", API_SERVICE_NAME);
                return null;
            }
            log.debug("Кол-во полученных городов из сервиса {}: {}.", API_SERVICE_NAME, locations.length);
            return Arrays.stream(locations).toList();
        } catch (RestClientException e) {
            String errorMessage = String.format("В ходе получения городов из сервиса %s произошла ошибка. Причина: %s.\nStackTrace: %s",
                    API_SERVICE_NAME, ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
            log.error(errorMessage);
            throw new IntegrationException(errorMessage, ExceptionUtils.getRootCause(e));
        }
    }
}

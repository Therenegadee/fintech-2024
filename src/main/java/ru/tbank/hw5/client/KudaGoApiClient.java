package ru.tbank.hw5.client;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.dto.KudaGoEventResponse;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.exception.IntegrationException;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import static ru.tbank.hw5.utils.ThreadUtils.acquireSemaphoreSafely;

@MethodExecutionTimeTracked
@Service
@Slf4j
public class KudaGoApiClient {

    public static final String API_SERVICE_NAME = "KudaGo";
    @Value("${kudago-api.base-url}")
    private String baseUrl;
    @Value("${kudago-api.categories-path}")
    private String placeCategoriesPath;
    @Value("${kudago-api.locations-path}")
    private String locationsPath;
    @Value("${kudago-api.events-path}")
    private String eventsPath;
    @Value("${kudago-api.parallel-requests.number}")
    private int parallelRequestNumber;
    @Value("${kudago-api.parallel-requests.fair-access}")
    private boolean parallelRequestFairAccess;

    private static final String ACTUAL_SINCE_QUERY_PARAM = "actual_since";
    private static final String ACTUAL_UNTIL_QUERY_PARAM = "actual_until";
    private static final String PAGE_SIZE_QUERY_PARAM = "page_size";

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final Semaphore semaphore;

    public KudaGoApiClient(RestTemplate restTemplate,
                           WebClient webClient,
                           @Value("${kudago-api.parallel-requests.number}") int parallelRequestNumber,
                           @Value("${kudago-api.parallel-requests.fair-access}") boolean parallelRequestFairAccess) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.semaphore = new Semaphore(parallelRequestNumber, parallelRequestFairAccess);
    }


    @Nullable
    public List<PlaceCategory> getAllPlaceCategories() {
        try {
            log.debug("Получение категорий мест из сервиса {}.", API_SERVICE_NAME);
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path(placeCategoriesPath)
                    .build()
                    .toUri();
            acquireSemaphoreSafely(semaphore);
            ResponseEntity<PlaceCategory[]> response = restTemplate
                    .getForEntity(uri, PlaceCategory[].class);
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
        } finally {
             semaphore.release();
        }
    }

    @Nullable
    public List<Location> getAllLocations() {
        try {
            log.debug("Получение городов из сервиса {}.", API_SERVICE_NAME);
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path(locationsPath)
                    .build()
                    .toUri();
            acquireSemaphoreSafely(semaphore);
            ResponseEntity<Location[]> response = restTemplate
                    .getForEntity(uri, Location[].class);
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
        } finally {
             semaphore.release();
        }
    }

    @Nullable
    public List<KudaGoEventResponse.EventDTO> getEvents(long dateFromTimestamp, long dateToTimestamp) {
        try {
            log.debug("Получение событий из сервиса {}.", API_SERVICE_NAME);
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path(eventsPath)
                    .queryParam(ACTUAL_SINCE_QUERY_PARAM, dateFromTimestamp)
                    .queryParam(ACTUAL_UNTIL_QUERY_PARAM, dateToTimestamp)
                    .queryParam(PAGE_SIZE_QUERY_PARAM, 10000)
                    .build()
                    .toUri();
            acquireSemaphoreSafely(semaphore);
            ResponseEntity<KudaGoEventResponse> response = restTemplate
                    .getForEntity(uri, KudaGoEventResponse.class);
            if (Objects.isNull(response.getBody()) || Objects.isNull(response.getBody().getEvents())) {
                log.error("Полученный список событий из сервиса {} был null!", API_SERVICE_NAME);
                return null;
            }
            List<KudaGoEventResponse.EventDTO> events = response.getBody().getEvents();
            log.debug("Кол-во полученных событий из сервиса {}: {}.", API_SERVICE_NAME, events.size());
            return events;
        } catch (RestClientException e) {
            String errorMessage = String.format("В ходе получения событий из сервиса %s произошла ошибка. Причина: %s.\nStackTrace: %s",
                    API_SERVICE_NAME, ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
            log.error(errorMessage);
            throw new IntegrationException(errorMessage, ExceptionUtils.getRootCause(e));
        } finally {
            semaphore.release();
        }
    }


    public Flux<KudaGoEventResponse.EventDTO> getEventsFlux(long dateFromTimestamp, long dateToTimestamp) {
        log.debug("Получение событий из сервиса {}.", API_SERVICE_NAME);
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path(eventsPath + "/")
                .queryParam(ACTUAL_SINCE_QUERY_PARAM, dateFromTimestamp)
                .queryParam(ACTUAL_UNTIL_QUERY_PARAM, dateToTimestamp)
                .queryParam(PAGE_SIZE_QUERY_PARAM, 10000)
                .build()
                .toUri();
        acquireSemaphoreSafely(semaphore);
        return webClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(KudaGoEventResponse.class)
                .map(response -> {
                    if (Objects.isNull(response.getBody()) || Objects.isNull(response.getBody().getEvents())) {
                        log.error("Полученный список событий из сервиса {} был null!", API_SERVICE_NAME);
                        return null;
                    }
                    List<KudaGoEventResponse.EventDTO> events = response.getBody().getEvents();
                    log.debug("Кол-во полученных событий из сервиса {}: {}.", API_SERVICE_NAME, events.size());
                    return events;
                })
                .flatMapIterable(list -> list)
                .doOnError((e) -> {
                    String errorMessage = String.format("В ходе получения событий из сервиса %s произошла ошибка. Причина: %s.\nStackTrace: %s",
                            API_SERVICE_NAME, ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
                    log.error(errorMessage);
                    throw new IntegrationException(errorMessage, ExceptionUtils.getRootCause(e));
                })
                .doOnTerminate(semaphore::release);
    }
}

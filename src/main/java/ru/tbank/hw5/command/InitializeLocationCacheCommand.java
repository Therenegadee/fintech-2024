package ru.tbank.hw5.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.service.LocationService;
import ru.tbank.hw5.service.PlaceCategoryService;

import java.util.List;
import java.util.Objects;

import static ru.tbank.hw5.client.KudaGoApiClient.API_SERVICE_NAME;

@MethodExecutionTimeTracked
@RequiredArgsConstructor
@Service
@Slf4j
public class InitializeLocationCacheCommand implements Command {

    private final LocationService locationService;
    private final KudaGoApiClient kudaGoApiClient;

    @Override
    public void execute() {
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
    }
}

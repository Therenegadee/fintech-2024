package ru.tbank.hw5.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.service.LocationService;
import ru.tbank.hw5.service.PlaceCategoryService;

import java.util.List;
import java.util.Objects;

import static ru.tbank.hw5.client.KudaGoApiClient.API_SERVICE_NAME;

@MethodExecutionTimeTracked
@RequiredArgsConstructor
@Service
@Slf4j
public class InitializePlaceCategoriesCacheCommand implements Command {

    private final PlaceCategoryService placeCategoryService;
    private final KudaGoApiClient kudaGoApiClient;

    @Override
    public void execute() {
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
    }
}

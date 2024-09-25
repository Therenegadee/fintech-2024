package ru.tbank.hw5.v1.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.hw5.annotation.ExecutionTimeObserved;
import ru.tbank.hw5.cache.PlaceCategoriesCache;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.exception.NotFoundException;
import ru.tbank.hw5.service.PlaceCategoryService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static ru.tbank.hw5.client.KudaGoApiClient.API_SERVICE_NAME;

@ExecutionTimeObserved
@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceCategoryServiceImpl implements PlaceCategoryService {

    private final KudaGoApiClient kudaGoApiClient;
    private final PlaceCategoriesCache placeCategoriesCache;

    @PostConstruct
    private List<PlaceCategory> initCache() {
        log.debug("Начало наполнения кэша категорий мест из сервиса {}.", API_SERVICE_NAME);
        List<PlaceCategory> placeCategories = kudaGoApiClient.getAllPlaceCategories();
        if (Objects.isNull(placeCategories)) {
            String errorDetails = String.format("Полученный список категорий мест из сервиса %s был null!", API_SERVICE_NAME);
            log.error(errorDetails);
            throw new IllegalStateException(errorDetails);
        }
        placeCategoriesCache.saveAll(placeCategories);
        log.debug("Кэш категорий мест из сервиса {} был успешно наполнен. Список полученных категорий мест содержал {} запись.", 
                API_SERVICE_NAME, placeCategories.size());
        return placeCategories;
    }
    
    @Override
    public List<PlaceCategory> getAllPlaceCategories() {
        log.info("Получение всех категорий мест из кэша.");
        return placeCategoriesCache.findAll();
    }

    @Override
    public void saveAll(List<PlaceCategory> placeCategories) {
        log.info("Начало сохранения списка категорий мест в кэш. Размер входного списка: {}.", placeCategories.size());
        placeCategoriesCache.saveAll(placeCategories);
        log.info("Список категорий мест был успешно сохранен в кэш.");
    }

    @Override
    public List<PlaceCategory> findAll() {
        log.info("Начало получения списка всех категорий мест из кэша.");
        List<PlaceCategory> placeCategories = placeCategoriesCache.findAll();
        log.info("Все категории мест из кэша были успешно получены. Размер списка: {}.", placeCategories.size());
        return placeCategories;
    }

    @Override
    public PlaceCategory findById(Integer id) {
        log.info("Начало получения категории места с идентификатором \"{}\" из кэша.", id);
        PlaceCategory placeCategory = placeCategoriesCache.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Категория места с идентификатором %d не была найдена!", id);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });;
        log.info("Категория места с идентификатором \"{}\" была успешно получена из кэша.", id);
        return placeCategory;
    }

    @Override
    public PlaceCategory save(PlaceCategory placeCategory) {
        log.info("Начало сохранения в кэш категории места с именем: \"{}\".", placeCategory.getName());
        placeCategory = placeCategoriesCache.save(placeCategory);
        log.info("Категория места с именем : \"{}\" была успешно сохранена в кэш с идентификатором {}.",
                placeCategory.getName(), placeCategory.getId());
        return placeCategory;
    }

    @Override
    public PlaceCategory update(Integer id, PlaceCategory placeCategory) {
        log.info("Начало обновления в кэше категории места с идентификатором: {}.", id);
        placeCategory = placeCategoriesCache.update(id, placeCategory);
        log.info("Категория места с идентификатором {} была успешно обновлена в кэше.", id);
        return placeCategory;
    }

    @Override
    public void delete(Integer id) {
        log.info("Начало удаления из кэша категории места с идентификатором: {}.", id);
        placeCategoriesCache.delete(id);
        log.info("Категория места с идентификатором {} была успешно удалена из кэша.", id);
    }
}

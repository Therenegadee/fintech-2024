package ru.tbank.hw5.v1.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.cache.PlaceCategoriesCache;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.exception.NotFoundException;
import ru.tbank.hw5.service.PlaceCategoryService;

import java.util.List;

@MethodExecutionTimeTracked
@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceCategoryServiceImpl implements PlaceCategoryService {

    private final PlaceCategoriesCache placeCategoriesCache;

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

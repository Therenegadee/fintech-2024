package ru.tbank.hw5.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.dto.PlaceCategory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@MethodExecutionTimeTracked
@Component
@Slf4j
@Getter
@Setter
public class PlaceCategoriesCache extends DataCache<PlaceCategory, Integer> {

    protected final AtomicInteger idCounter = new AtomicInteger(1);
    private static final ReentrantLock LOCK = new ReentrantLock(true);

    @Override
    public void saveAll(List<PlaceCategory> placeCategories) {
        log.info("Сохранение списка категорий мест в кэш.");
        placeCategories.forEach(this::save);
        log.info("Список категорий мест успешно сохранен в кэш.");
    }

    @Override
    public List<PlaceCategory> findAll() {
        log.info("Получение всех категорий мест из кэша.");
        if (cache.isEmpty()) {
            return Collections.emptyList();
        }
        return cache.values().stream().toList();
    }

    @Override
    public Optional<PlaceCategory> findById(Integer id) {
        log.info("Получение категории места по идентификатору \"{}\" из кэша.", id);
        if (cache.containsKey(id)) {
            log.info("Категория места с идентификатором \"{}\" была успешно получена из кэша.", id);
            return Optional.of(cache.get(id));
        }
        return Optional.empty();
    }

    @Override
    public PlaceCategory save(PlaceCategory placeCategory) {
        log.info("Сохранение категории места в кэш. Slug места: {}.", placeCategory.getName());
        try {
            LOCK.lock();
            Integer savedId = idCounter.getAndIncrement();
            placeCategory.setId(savedId);
            cache.put(savedId, placeCategory);
            log.info("Категория места (slug: {}) была успешно сохранена в кэш с идентификатором: {}.",
                    placeCategory.getSlug(), savedId);
            return placeCategory;
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public PlaceCategory update(Integer id, PlaceCategory updatedPlaceCategory) {
        log.info("Обновление категории места с идентификатором \"{}\" в кэше.", id);
        try {
            LOCK.lock();
            if (!cache.containsKey(id)) {
                String errorMessage = String.format("Категории места с идентификатором \"%s\" не существует в кэше!", id);
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            PlaceCategory cachedPlaceCategory = cache.get(id);
            cachedPlaceCategory.setName(updatedPlaceCategory.getName());
            cachedPlaceCategory.setSlug(updatedPlaceCategory.getSlug());
            log.info("Категория места с идентификатором \"{}\" была успешно обновлена в кэше. Новые значения: (slug: {}, name: {})", id,
                    cachedPlaceCategory.getName(), cachedPlaceCategory.getSlug());
            return cachedPlaceCategory;
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void delete(Integer id) {
        log.info("Удаление категории места с идентификатором \"{}\" из кэша.", id);
        try {
            LOCK.lock();
            if (!cache.containsKey(id)) {
                String errorMessage = String.format("Категории места с идентификатором \"%s\" не существует в кэше и она не может быть удалена!",
                        id);
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            cache.remove(id);
            log.info("Категория места с идентификатором \"{}\" была успешно удалена из кэша.", id);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}

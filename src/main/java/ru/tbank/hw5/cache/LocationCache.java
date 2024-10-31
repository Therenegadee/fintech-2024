package ru.tbank.hw5.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.observer.LocationUpdateObserver;
import ru.tbank.hw5.observer.Observable;
import ru.tbank.hw5.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@MethodExecutionTimeTracked
@Component
@Slf4j
@Getter
@Setter
public class LocationCache extends DataCache<Location, String> implements Observable<Location> {

    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private final List<Observer<Location>> observers = new ArrayList<>();

    public LocationCache(LocationUpdateObserver locationUpdateObserver) {
        observers.add(locationUpdateObserver);
    }

    @Override
    public void addObserver(Observer<Location> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<Location> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Location entity) {
        for (Observer<Location> observer : observers) {
            observer.update(entity);
        }
    }

    @Override
    public void saveAll(List<Location> locations) {
        log.info("Сохранение списка городов в кэш.");
        locations.forEach(this::save);
        log.info("Список городов успешно сохранен в кэш.");
    }

    @Override
    public List<Location> findAll() {
        log.info("Получение всех городов из кэша.");
        return cache.values().stream().toList();
    }

    @Override
    public Optional<Location> findById(String slug) {
        log.info("Получение города по slug \"{}\" из кэша.", slug);
        if (cache.containsKey(slug)) {
            log.info("Город (slug :\"{}\") был успешно получен из кэша.", slug);
            return Optional.of(cache.get(slug));
        }
        return Optional.empty();
    }

    @Override
    public Location save(Location location) {
        String slug = location.getSlug();
        log.info("Сохранение города в кэш. Slug сохраняемого города: {}.", slug);
        try {
            LOCK.lock();
            if (cache.containsKey(slug)) {
                String errorMessage = String.format("Город со slug \"%s\" уже присутствует в кэше!", slug);
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            location.setSlug(slug);
            cache.put(slug, location);
            notifyObservers(location);
            log.info("Город (slug: {}, name: {}) была успешно сохранена в кэш.", slug,
                    location.getName());
            return location;
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public Location update(String slug, Location updatedLocation) {
        log.info("Обновление города со slug \"{}\" в кэше.", slug);
        try {
            LOCK.lock();
            if (!cache.containsKey(slug)) {
                String errorMessage = String.format("Города со slug \"{}\" не существует в кэше!", slug);
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            Location cachedLocation = cache.get(slug);
            notifyObservers(cachedLocation);
            cachedLocation.setName(updatedLocation.getName());
            cachedLocation.setSlug(updatedLocation.getSlug());
            log.info("Город со slug  \"{}\" был успешно обновлен в кэше. Новые значения: (name: {})", slug,
                    cachedLocation.getName());
            notifyObservers(cachedLocation);
            return cachedLocation;
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void delete(String slug) {
        log.info("Удаление города со slug \"{}\" из кэша.", slug);
        try {
            LOCK.lock();
            if (!cache.containsKey(slug)) {
                String errorMessage = String.format("Города со slug \"%s\" не существует в кэше и он не может быть удален!", slug);
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            notifyObservers(cache.get(slug));
            cache.remove(slug);
            log.info("Город со slug \"{}\" был успешно удален из кэша.", slug);
        } finally {
            LOCK.unlock();
        }
    }
}

package ru.tbank.hw5.cache;

import ru.tbank.hw5.dto.PlaceCategory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DataCache<T, ID> {

    protected final ConcurrentHashMap<ID, T> cache = new ConcurrentHashMap<>();

    abstract void saveAll(List<T> data);

    abstract List<T> findAll();

    abstract Optional<T> findById(ID id);

    abstract T save(T data);

    abstract T update(ID id, T data);
    abstract void delete(ID id);
}

package ru.tbank.hw5.cache;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DataCache<V, K> {

    protected final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    abstract void saveAll(List<V> data);

    abstract List<V> findAll();

    abstract Optional<V> findById(K id);

    abstract V save(V data);

    abstract V update(K id, V data);
    abstract void delete(K id);

    abstract void clearCache();
}

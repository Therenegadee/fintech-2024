package ru.tbank.hw5.cache;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.TreeMap;

public abstract class SnapshotCache<V> {

    protected final TreeMap<OffsetDateTime, V> cache = new TreeMap<>();

    List<V> findAllByPeriod(OffsetDateTime startPeriod, OffsetDateTime endPeriod) {
        return cache.subMap(startPeriod, true, endPeriod, true)
                .values()
                .stream()
                .toList();
    }

    abstract V save(V data);
}

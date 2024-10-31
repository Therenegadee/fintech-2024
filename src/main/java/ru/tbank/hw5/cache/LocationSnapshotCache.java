package ru.tbank.hw5.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tbank.hw5.snapshot.LocationSnapshot;

@Component
@Slf4j
public class LocationSnapshotCache extends SnapshotCache<LocationSnapshot> {
    @Override
    public LocationSnapshot save(LocationSnapshot data) {
        log.info("Сохранение снимка данных о локации (name = {}, slug = {}). Snapshot TimeStamp: {}.",
                data.getName(), data.getSlug(), data.getSnapshotTime());
        cache.put(data.getSnapshotTime(), data);
        return data;
    }
}

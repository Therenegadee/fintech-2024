package ru.tbank.hw5.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tbank.hw5.snapshot.PlaceCategorySnapshot;

@Component
@Slf4j
public class PlaceCategorySnapshotCache extends SnapshotCache<PlaceCategorySnapshot> {

    @Override
    public PlaceCategorySnapshot save(PlaceCategorySnapshot data) {
        log.info("Сохранение снимка данных о локации (id = {}, name = {}, slug = {}). Snapshot TimeStamp: {}.",
                data.getId(), data.getName(), data.getSlug(), data.getSnapshotTime());
        cache.put(data.getSnapshotTime(), data);
        return data;
    }

}

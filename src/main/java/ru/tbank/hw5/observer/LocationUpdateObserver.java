package ru.tbank.hw5.observer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tbank.hw5.cache.LocationSnapshotCache;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.snapshot.LocationSnapshot;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class LocationUpdateObserver implements Observer<Location> {

    private final LocationSnapshotCache locationSnapshotCache;

    @Override
    public void update(Location entity) {
        LocationSnapshot snapshot = LocationSnapshot.builder()
                .name(entity.getName())
                .slug(entity.getSlug())
                .snapshotTime(OffsetDateTime.now())
                .build();
        locationSnapshotCache.save(snapshot);
    }
}

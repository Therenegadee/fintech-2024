package ru.tbank.hw5.observer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tbank.hw5.cache.PlaceCategorySnapshotCache;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.service.PlaceCategoryService;
import ru.tbank.hw5.snapshot.PlaceCategorySnapshot;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class PlaceCategoryUpdateObserver implements Observer<PlaceCategory> {

    private final PlaceCategorySnapshotCache placeCategorySnapshotCache;

    @Override
    public void update(PlaceCategory entity) {
        PlaceCategorySnapshot snapshot = PlaceCategorySnapshot.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .snapshotTime(OffsetDateTime.now())
                .build();
        placeCategorySnapshotCache.save(snapshot);
    }
}

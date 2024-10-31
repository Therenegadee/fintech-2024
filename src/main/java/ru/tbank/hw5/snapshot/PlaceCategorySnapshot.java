package ru.tbank.hw5.snapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceCategorySnapshot {
    private Integer id;
    private String slug;
    private String name;
    private OffsetDateTime snapshotTime;
}

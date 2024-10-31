package ru.tbank.hw5.snapshot;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationSnapshot {
    private String slug;
    private String name;
    private OffsetDateTime snapshotTime;
}

package ru.tbank.hw10.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class EventDto {
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private OffsetDateTime fromDate;

    @NotNull
    private OffsetDateTime toDate;

    @NotNull
    private Double price;

    @NotNull
    private Integer placeId;
}

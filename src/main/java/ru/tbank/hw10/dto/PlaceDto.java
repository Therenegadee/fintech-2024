package ru.tbank.hw10.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class PlaceDto {

    private Integer id;

    @NotNull
    private String slug;

    @NotNull
    private String name;
}

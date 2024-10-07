package ru.tbank.hw5.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private String slug;
    private String name;
}

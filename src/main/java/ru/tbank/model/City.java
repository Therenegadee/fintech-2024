package ru.tbank.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City {
    private String slug; // ???
    @JsonProperty("coords")
    @JacksonXmlProperty(localName = "coordinates")
    private Coordinates coordinates;

    @Override
    public String toString() {
        return String.format("Город [Абберавиатура: %s; Координаты: %s.]", slug, coordinates);
    }
}

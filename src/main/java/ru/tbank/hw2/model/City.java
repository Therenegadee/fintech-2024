package ru.tbank.hw2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

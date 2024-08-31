package ru.tbank.model;

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
public class Coordinates {
    @JsonProperty("lat")
    @JacksonXmlProperty(localName = "latitude")
    private double latitude;
    @JsonProperty("lon")
    @JacksonXmlProperty(localName = "longitude")
    private double longitude;

    @Override
    public String toString() {
        return String.format("%s ш., %s д.", latitude, longitude);
    }
}

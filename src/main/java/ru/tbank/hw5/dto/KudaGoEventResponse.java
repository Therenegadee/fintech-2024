package ru.tbank.hw5.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KudaGoEventResponse {
    @JsonProperty("results")
    private List<EventDTO> events;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventDTO {
        private Long id;
        private String title;
        private String price;
        private boolean isFree;
    }
}

package ru.tbank.hw10.v1.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tbank.hw10.dto.EventDto;
import ru.tbank.hw10.entity.Event;
import ru.tbank.hw10.entity.Place;
import ru.tbank.hw10.exception.NotFoundException;
import ru.tbank.hw10.mapper.EventsMapper;
import ru.tbank.hw10.repository.EventRepository;
import ru.tbank.hw10.repository.PlaceRepository;
import ru.tbank.hw10.v1.service.EventService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class EventsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventsMapper eventMapper;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Place place;

    @Container
    public static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test-db")
            .withUsername("test-user")
            .withPassword("test-password");

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDB::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDB::getUsername);
        registry.add("spring.datasource.password", postgresDB::getPassword);
    }

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        placeRepository.deleteAll();
        place = Place.builder()
                .name("Place-1")
                .slug("place1")
                .build();
        place = placeRepository.save(place);
    }

    @Test
    void getAllEventsTest_databaseHasData_returnsAllResults() throws Exception {
        // Given
        Event event1 = Event.builder()
                .name("Event-1")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(300.00)
                .place(place)
                .build();
        Event event2 = Event.builder()
                .name("Event-2")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(400.00)
                .place(place)
                .build();
        event1 = eventRepository.save(event1);
        event2 = eventRepository.save(event2);

        List<EventDto> expectedResponse = Stream.of(event1, event2)
                .map(eventMapper::toDto)
                .toList();

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/events/all"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        List<EventDto> fetchedEntities = objectMapper.readValue(jsonResponse, new TypeReference<List<EventDto>>() {
        });

        // Then
        assertThat(fetchedEntities).isNotNull();
        assertThat(fetchedEntities).hasSize(2);
        assertThat(fetchedEntities).containsExactlyInAnyOrderElementsOf(expectedResponse);
    }

    @Test
    void getAllEventsTest_databaseHasNoData_returnsHttp404NotFound() throws Exception {
        // Given
        // When
        MvcResult result = mockMvc.perform(get("/api/v1/events/all"))
                .andExpect(status().isNotFound())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        // Then
        assertThat(jsonResponse).contains("В Базе Данных нет никакой информации о событиях.");
    }

    @Test
    void getAllEventsFilteredTest_databaseHasSuitableData_returnsValidResponse() throws Exception {
        // Given
        Event suitableEvent = Event.builder()
                .name("Event-1")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(300.00)
                .place(place)
                .build();

        Event notSuitableEvent = Event.builder()
                .name("Event-2")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(300.00)
                .place(place)
                .build();

        suitableEvent = eventRepository.save(suitableEvent);
        notSuitableEvent = eventRepository.save(notSuitableEvent);
        // When
        MvcResult result = mockMvc.perform(get("/api/v1/events")
                        .queryParam("placeId", String.valueOf(place.getId()))
                        .queryParam("eventName", suitableEvent.getName())
                )
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        List<EventDto> fetchedEntities = objectMapper.readValue(jsonResponse, new TypeReference<List<EventDto>>() {
        });

        // Then
        assertThat(fetchedEntities).hasSize(1);
        assertThat(fetchedEntities).containsOnly(eventMapper.toDto(suitableEvent));
    }

    @Test
    void createEventTest_validCreationRequest_returnsValidResult() throws Exception {
        // Given
        EventDto event = EventDto.builder()
                .name("Event-1")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(300.00)
                .placeId(place.getId())
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/events")
                        .content(objectMapper.writeValueAsString(event))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        EventDto savedEntity = objectMapper.readValue(jsonResponse, EventDto.class);

        // Then
        assertThat(savedEntity.getId()).isNotNull();
    }

    @Test
    void getEventByIdTest_databaseHasEventWithThatId_returnsValidResponse() throws Exception {
        // Given
        Event event1 = Event.builder()
                .name("Event-1")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(300.00)
                .place(place)
                .build();
        event1 = eventRepository.save(event1);

        Event event2 = Event.builder()
                .name("Event-2")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(400.00)
                .place(place)
                .build();
        event2 = eventRepository.save(event2);

        EventDto expectedResponse = eventMapper.toDto(event1);

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/events/{eventId}", event1.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        EventDto eventDto = objectMapper.readValue(jsonResponse, EventDto.class);

        // Then
        assertThat(eventDto).isNotNull();
        assertThat(eventDto).isEqualTo(expectedResponse);
    }

    @Test
    void getEventByIdTest_databaseHasNoEventWithThatId_returnsHttp404NotFound() throws Exception {
        // Given
        Event event1 = Event.builder()
                .name("Event-1")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(300.00)
                .place(place)
                .build();
        event1 = eventRepository.save(event1);

        int incorrectEventId = 239430;

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/events/{eventId}", Integer.toString(incorrectEventId)))
                .andExpect(status().isNotFound())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        // Then
        assertThat(jsonResponse).contains("Информация о событии с идентификатором " + incorrectEventId + " не была найдена.");
    }

    @Test
    void deleteEventByIdTest_databaseHasNoRecordWithId_returnHttp400BadRequest() throws Exception {
        // Given
        Event event1 = Event.builder()
                .name("Event-1")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(300.00)
                .place(place)
                .build();
        event1 = eventRepository.save(event1);

        int incorrectEventId = 239430;

        // When
        MvcResult result = mockMvc.perform(delete("/api/v1/events/{eventId}", Integer.toString(incorrectEventId)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        // Then
        assertThat(jsonResponse).contains("Информации о событии с идентификатором " + incorrectEventId + " не существует!");
    }

    @Test
    void deleteEventByIdTest_databaseHasRecordWithId_returnHttp200OkAndWhileFetchingDeletedEntityThrowsNotFoundException() throws Exception {
        // Given
        Event event = Event.builder()
                .name("Event-1")
                .fromDate(OffsetDateTime.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC)))
                .toDate(OffsetDateTime.from(LocalDate.of(2024, 12, 31).atStartOfDay(ZoneOffset.UTC)))
                .price(300.00)
                .place(place)
                .build();
        event = eventRepository.save(event);
        final int eventId = event.getId();

        // When
        // Then
        mockMvc.perform(delete("/api/v1/events/{eventId}", eventId))
                .andExpect(status().isOk());

        assertThatThrownBy(() -> eventService.findEventById(eventId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Информация о событии с идентификатором " + eventId + " не была найдена.");
    }
}

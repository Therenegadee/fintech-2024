package ru.tbank.hw10.v1.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tbank.hw10.dto.PlaceDto;
import ru.tbank.hw10.entity.Place;
import ru.tbank.hw10.exception.NotFoundException;
import ru.tbank.hw10.mapper.PlaceMapper;
import ru.tbank.hw10.repository.PlaceRepository;
import ru.tbank.hw10.v1.service.PlaceService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private PlaceMapper placeMapper;

    @Autowired
    private ObjectMapper objectMapper;

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
        placeRepository.deleteAll();
    }

    @Test
    void getAllPlacesTest_databaseHasData_returnsAllResults() throws Exception {
        // Given
        Place place1 = Place.builder()
                .name("Place-1")
                .slug("place1")
                .build();
        Place place2 = Place.builder()
                .name("Place-2")
                .slug("place2")
                .build();
        place1 = placeRepository.save(place1);
        place2 = placeRepository.save(place2);

        List<PlaceDto> expectedResponse = Stream.of(place1, place2)
                .map(placeMapper::toDto)
                .toList();

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/places/all"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        List<PlaceDto> fetchedEntities = objectMapper.readValue(jsonResponse, new TypeReference<List<PlaceDto>>() {
        });

        // Then
        assertThat(fetchedEntities).isNotNull();
        assertThat(fetchedEntities).hasSize(2);
        assertThat(fetchedEntities).containsExactlyInAnyOrderElementsOf(expectedResponse);
    }

    @Test
    void getAllPlacesTest_databaseHasNoData_returnsHttp404NotFound() throws Exception {
        // Given
        // When
        MvcResult result = mockMvc.perform(get("/api/v1/places/all"))
                .andExpect(status().isNotFound())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        // Then
        assertThat(jsonResponse).contains("В Базе Данных нет никакой информации о местах.");
    }

    @Test
    void createPlaceTest_validCreationRequest_returnsValidResult() throws Exception {
        // Given
        PlaceDto place = PlaceDto.builder()
                .name("Place-1")
                .slug("place1")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/places")
                        .content(objectMapper.writeValueAsString(place))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        PlaceDto savedEntity = objectMapper.readValue(jsonResponse, PlaceDto.class);

        // Then
        assertThat(savedEntity.getId()).isNotNull();
    }

    @Test
    void getPlaceByIdTest_databaseHasPlaceWithThatId_returnsValidResponse() throws Exception {
        // Given
        Place place1 = Place.builder()
                .name("Place-1")
                .slug("place1")
                .build();
        place1 = placeRepository.save(place1);

        Place place2 = Place.builder()
                .name("Place-2")
                .slug("place2")
                .build();
        place2 = placeRepository.save(place2);

        PlaceDto expectedResponse = placeMapper.toDto(place1);

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/places/{placeId}", place1.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        PlaceDto placeDto = objectMapper.readValue(jsonResponse, PlaceDto.class);

        // Then
        assertThat(placeDto).isNotNull();
        assertThat(placeDto).isEqualTo(expectedResponse);
    }

    @Test
    void getPlaceByIdTest_databaseHasNoPlaceWithThatId_returnsHttp404NotFound() throws Exception {
        // Given
        Place place1 = Place.builder()
                .name("Place-1")
                .slug("place1")
                .build();
        place1 = placeRepository.save(place1);

        int incorrectPlaceId = 239430;

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/places/{placeId}", Integer.toString(incorrectPlaceId)))
                .andExpect(status().isNotFound())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        // Then
        assertThat(jsonResponse).contains("Информация о месте с идентификатором " + incorrectPlaceId + " не была найдена.");
    }

    @Test
    void deletePlaceByIdTest_databaseHasNoRecordWithId_returnHttp400BadRequest() throws Exception {
        // Given
        Place place1 = Place.builder()
                .name("Place-1")
                .slug("place1")
                .build();
        place1 = placeRepository.save(place1);

        int incorrectPlaceId = 239430;

        // When
        MvcResult result = mockMvc.perform(delete("/api/v1/places/{placeId}", Integer.toString(incorrectPlaceId)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        // Then
        assertThat(jsonResponse).contains("Информации о месте с идентификатором " + incorrectPlaceId + " не существует!");
    }

    @Test
    void deletePlaceByIdTest_databaseHasRecordWithId_returnHttp200OkAndWhileFetchingDeletedEntityThrowsNotFoundException() throws Exception {
        // Given
        Place place = Place.builder()
                .name("Place-1")
                .slug("place")
                .build();
        place = placeRepository.save(place);
        final int placeId = place.getId();

        // When
        // Then
        mockMvc.perform(delete("/api/v1/places/{placeId}", placeId))
                .andExpect(status().isOk());

        assertThatThrownBy(() -> placeService.findPlaceById(placeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Информация о месте с идентификатором " + placeId + " не была найдена.");
    }
}

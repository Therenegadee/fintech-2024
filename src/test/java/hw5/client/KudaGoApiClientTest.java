package hw5.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.reflect.FieldUtils;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import ru.tbank.HomeworkApplication;
import ru.tbank.hw5.cache.CacheInitializer;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.exception.IntegrationException;

import java.lang.reflect.Field;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = HomeworkApplication.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.yml")
@Testcontainers
public class KudaGoApiClientTest {

    @Container
    private static WireMockContainer wireMock = new WireMockContainer(WireMockContainer.OFFICIAL_IMAGE_NAME);

    @Autowired
    private KudaGoApiClient kudaGoApiClient;

    @Value("${kudago-api.categories-path}")
    private String placeCategoriesPath;
    @Value("${kudago-api.locations-path}")
    private String locationsPath;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CacheInitializer cacheInitializer;


    @DynamicPropertySource
    public static void dynamicProperty(DynamicPropertyRegistry registry) {
        registry.add("kudago-api.base-url", wireMock::getBaseUrl);
    }

    @BeforeAll
    public static void setup() {
        WireMock.configureFor(wireMock.getHost(), wireMock.getFirstMappedPort());
    }

    @Test
    void testGetAllPlaceCategories_correctPath_success() {
        // Given
        List<PlaceCategory> expectedPlaceCategories = List.of(
                PlaceCategory.builder().id(1).name("Place-1").build(),
                PlaceCategory.builder().id(2).name("Place-2").build(),
                PlaceCategory.builder().id(3).name("Place-3").build()
        );
        stubFor(get(urlEqualTo(placeCategoriesPath))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                        .withBody(Json.write(expectedPlaceCategories))
                ));

        // When
        List<PlaceCategory> placeCategories = kudaGoApiClient.getAllPlaceCategories();

        // Then
        assertThat(placeCategories).hasSameSizeAs(expectedPlaceCategories);
        assertThat(placeCategories).hasSameElementsAs(expectedPlaceCategories);
    }

    @Test
    void testGetAllPlaceCategories_responseIsNull_returnsNull() {
        // Given
        String nullResponse = null;
        stubFor(get(urlEqualTo(placeCategoriesPath))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                        .withBody(nullResponse)
                ));

        // When
        List<PlaceCategory> placeCategories = kudaGoApiClient.getAllPlaceCategories();

        // Then
        assertThat(placeCategories).isNull();
    }

    @Test
    void testGetAllLocations_correctPath_success() {
        // Given
        List<Location> expectedLocations = List.of(
                Location.builder().slug("Slug-1").name("Location-1").build(),
                Location.builder().slug("Slug-2").name("Location-2").build(),
                Location.builder().slug("Slug-3").name("Location-3").build()
        );
        stubFor(get(urlEqualTo(locationsPath))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                        .withBody(Json.write(expectedLocations))
                ));

        // When
        List<Location> locations = kudaGoApiClient.getAllLocations();

        // Then
        assertThat(locations).hasSameSizeAs(expectedLocations);
        assertThat(locations).hasSameElementsAs(expectedLocations);
    }

    @Test
    void testGetAllLocations_responseIsNull_returnsNull() {
        // Given
        String nullResponse = null;
        stubFor(get(urlEqualTo(locationsPath))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                        .withBody(Json.write(nullResponse))
                ));

        // When
        List<Location> locations = kudaGoApiClient.getAllLocations();

        // Then
        assertThat(locations).isNull();
    }


}

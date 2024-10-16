package ru.tbank.hw5.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.common.Json;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import ru.tbank.HomeworkApplication;
import ru.tbank.hw5.cache.CacheInitializer;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.KudaGoEventResponse;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.exception.BadRequestException;
import ru.tbank.hw5.exception.IntegrationException;
import ru.tbank.hw5.exception.NotFoundException;
import ru.tbank.hw5.exception.RestTemplateResponseErrorHandler;
import ru.tbank.hw5.interceptor.RestClientLoggingRequestInterceptor;

import java.net.URI;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.testcontainers.shaded.com.google.common.base.CharMatcher.any;

@SpringBootTest(classes = HomeworkApplication.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.yml")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KudaGoApiClientTest {

    @Container
    private static WireMockContainer wireMock = new WireMockContainer(WireMockContainer.OFFICIAL_IMAGE_NAME);

    @Autowired
    private KudaGoApiClient kudaGoApiClient;
    @SpyBean(proxyTargetAware = false)
    private RestTemplate restTemplate;
    @Autowired
    private RestTemplateResponseErrorHandler responseErrorHandler;
    @Autowired
    private RestClientLoggingRequestInterceptor requestLoggingInterceptor;

    @Value("${kudago-api.categories-path}")
    private String placeCategoriesPath;
    @Value("${kudago-api.locations-path}")
    private String locationsPath;
    @Value("${kudago-api.events-path}")
    private String eventsPath;
    @Value("${kudago-api.parallel-requests.number}")
    private int parallelRequestNumber;

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

    @BeforeEach
    public void resetStubs() {
        WireMock.resetToDefault();
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


    public static Stream<Arguments> httpCodesSuccessCases() {
        return Stream.of(
                Arguments.of(HttpStatus.NOT_FOUND, new NotFoundException("Запрошенный ресурс не был найден.")),
                Arguments.of(HttpStatus.BAD_REQUEST, new BadRequestException("Были переданы некорректные параметры.")),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, new IntegrationException("Произошла ошибка на стороне вызываемого сервиса."))
        );
    }

    @ParameterizedTest
    @MethodSource("httpCodesSuccessCases")
    void testGetAllPlaceCategories_unsuccessfulCodesExceptionHandling(HttpStatus status, Exception expectedException) {
        // Given
        stubFor(get(urlEqualTo(placeCategoriesPath))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                ));

        // When
        // Assert
        assertThatThrownBy(() -> kudaGoApiClient.getAllPlaceCategories())
                .isInstanceOf(expectedException.getClass());
    }

    @ParameterizedTest
    @MethodSource("httpCodesSuccessCases")
    void testGetAllLocations_unsuccessfulCodes_correctlyHandled(HttpStatus status, Exception expectedException) {
        // Given
        stubFor(get(urlEqualTo(placeCategoriesPath))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                ));

        // When
        // Assert
        assertThatThrownBy(() -> kudaGoApiClient.getAllPlaceCategories())
                .isInstanceOf(expectedException.getClass());
    }

    @Test
    void testGetEvents_numberOfThreadsIsTwoTimesBiggerThanParallelRequestsAllowed_semaphoreWorksCorrectly() throws InterruptedException {
        // Given
        KudaGoEventResponse sampleResponse = KudaGoEventResponse.builder()
                .events(List.of(
                                KudaGoEventResponse.EventDTO.builder()
                                        .id(1L)
                                        .title("Title")
                                        .price("123")
                                        .isFree(false)
                                        .build()
                        )
                )
                .build();
        long dateFromTimestamp = 1L;
        long dateToTimestamp = 2L;
        stubFor(get(urlPathMatching("^/events(\\?.*)?$"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                        .withBody(Json.write(sampleResponse))
                ));
        int numberOfThreads = parallelRequestNumber * 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(parallelRequestNumber);

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    if (finalI >= parallelRequestNumber) {
                        Thread.sleep(Duration.ofSeconds(1L));
                    }
                    kudaGoApiClient.getEvents(dateFromTimestamp, dateToTimestamp);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // Then
        verify(restTemplate, times(parallelRequestNumber))
                .getForEntity(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(KudaGoEventResponse.class));
    }
}

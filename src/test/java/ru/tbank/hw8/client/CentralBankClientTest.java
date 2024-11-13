package ru.tbank.hw8.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import ru.tbank.HomeworkApplication;
import ru.tbank.hw8.client.CentralBankClient;
import ru.tbank.hw8.dto.CurrencyRate;
import ru.tbank.hw8.exception.ServiceUnavailableException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = HomeworkApplication.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.yml")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CentralBankClientTest {

    @Container
    private static WireMockContainer wireMock = new WireMockContainer(WireMockContainer.OFFICIAL_IMAGE_NAME);

    @Autowired
    private CentralBankClient centralBankClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private CacheManager cacheManager;

    @Value("${spring.cache.cache-names}")
    private String cacheName;

    @Value("${cbr-api.currency.daily-exchange-rates.path}")
    private String currencyExchangeRatesPath;

    @Value("${cbr-api.currency.daily-exchange-rates.date-param}")
    private String dateQueryParam;

    private static final LocalDate SEARCH_DATE = LocalDate.of(2024, 10, 4);
    private static final String CIRCUIT_BREAKER_NAME = "central-bank-client";

    @BeforeAll
    public static void beforeAllSetup() {
        WireMock.configureFor(wireMock.getHost(), wireMock.getFirstMappedPort());
    }

    @BeforeEach
    public void setup() {
        WireMock.resetToDefault();
        Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME).reset();
    }

    private void clearCache() {

    }

    @DynamicPropertySource
    public static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("cbr-api.base-url", wireMock::getBaseUrl);
    }

    @Test
    @Order(1)
    void testGetExchangeRates_correctXmlResponse_returnsExchangeRatesMap() {
        // Given
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateParam = SEARCH_DATE.format(formatter);
        String urlPath = String.format("%s?%s=%s", currencyExchangeRatesPath, dateQueryParam, dateParam);
        stubFor(get(urlEqualTo(urlPath))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                MediaType.APPLICATION_XML_VALUE)
                        .withBody(""" 
                                <ValCurs Date="04.10.2024" name="Foreign Currency Market">
                                    <Valute ID="R01375">
                                        <NumCode>156</NumCode>
                                        <CharCode>CNY</CharCode>
                                        <Nominal>1</Nominal>
                                        <Name>Китайский юань</Name>
                                        <Value>13,4808</Value>
                                        <VunitRate>13,4808</VunitRate>
                                    </Valute>
                                    <Valute ID="R01820">
                                        <NumCode>392</NumCode>
                                        <CharCode>JPY</CharCode>
                                        <Nominal>100</Nominal>
                                        <Name>Японских иен</Name>
                                        <Value>64,6349</Value>
                                        <VunitRate>0,646349</VunitRate>
                                    </Valute>
                                </ValCurs>""")));

        // When
        Map<String, CurrencyRate> currencyRatesByCode = centralBankClient.getCurrenciesExchangeRates(SEARCH_DATE);

        // Then
        assertThat(currencyRatesByCode.size()).isEqualTo(3);
        assertThat(currencyRatesByCode.containsKey("CNY")).isTrue();
        assertThat(currencyRatesByCode.containsKey("JPY")).isTrue();
        assertThat(currencyRatesByCode.containsKey("RUB")).isTrue();
    }

    @Test
    @Order(2)
    void testGetExchangeRates_centralBankUnavailable_circuitBreakerFallbackMethodThrowsServiceUnavailableException() {
        // Given
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateParam = SEARCH_DATE.format(formatter);
        String urlPath = String.format("%s?%s=%s", currencyExchangeRatesPath, dateQueryParam, dateParam);

        stubFor(get(urlEqualTo(urlPath)).willReturn(aResponse().withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));
        circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME).transitionToOpenState();

        // When
        // Then
        assertThatThrownBy(() -> centralBankClient.getCurrenciesExchangeRates(SEARCH_DATE))
                .isInstanceOf(ServiceUnavailableException.class);
        assertThat(circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME).getState())
                .isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @Order(3)
    void testGetExchangeRates_centralBankAvailableAndSendsSuccessfulResponses_circuitBreakerHasClosedState() {
        // Given
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateParam = SEARCH_DATE.format(formatter);
        String urlPath = String.format("%s?%s=%s", currencyExchangeRatesPath, dateQueryParam, dateParam);

        stubFor(get(urlEqualTo(urlPath))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                MediaType.APPLICATION_XML_VALUE)
                        .withBody(""" 
                                <ValCurs Date="04.10.2024" name="Foreign Currency Market">
                                    <Valute ID="R01375">
                                        <NumCode>156</NumCode>
                                        <CharCode>CNY</CharCode>
                                        <Nominal>1</Nominal>
                                        <Name>Китайский юань</Name>
                                        <Value>13,4808</Value>
                                        <VunitRate>13,4808</VunitRate>
                                    </Valute>
                                    <Valute ID="R01820">
                                        <NumCode>392</NumCode>
                                        <CharCode>JPY</CharCode>
                                        <Nominal>100</Nominal>
                                        <Name>Японских иен</Name>
                                        <Value>64,6349</Value>
                                        <VunitRate>0,646349</VunitRate>
                                    </Valute>
                                </ValCurs>""")));

        // When
        for (int i = 0; i < 5; i++) {
            centralBankClient.getCurrenciesExchangeRates(SEARCH_DATE);
            clearCache();
        }

        // Then
        assertThat(circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME).getState())
                .isEqualTo(CircuitBreaker.State.CLOSED);
    }
}

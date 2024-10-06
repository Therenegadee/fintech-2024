package ru.tbank.hw8.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.tbank.hw8.client.CentralBankClient;
import ru.tbank.hw8.dto.CurrencyConvertRequest;
import ru.tbank.hw8.dto.CurrencyConvertResponse;
import ru.tbank.hw8.dto.CurrencyRate;
import ru.tbank.hw8.dto.CurrencyRateResponse;
import ru.tbank.hw8.exception.ExceptionsHandler;
import ru.tbank.hw8.v1.service.CurrencyServiceImpl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CurrencyController.class})
public class CurrencyControllerTest {

    @MockBean
    private CentralBankClient centralBankClient;

    @MockBean
    private CurrencyServiceImpl currencyService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(currencyService, "centralBankClient", centralBankClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "eur", "CNY", "AUD", "Rub", "jPy"})
    public void testGetCurrencyRateByCode_validCurrencyCode_returnsHttp200Ok(String currencyCode) throws Exception {
        // Given
        when(currencyService.getCurrencyRateByCode(currencyCode))
                .thenReturn(new CurrencyRateResponse(currencyCode, BigDecimal.valueOf(Math.random())));

        // When
        // Then
        mockMvc.perform(get("/currencies/rates/{currencyCode}", currencyCode))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RANDOM", "CURRENCY", "CODE", "WTF"})
    public void testGetCurrencyRateByCode_invalidCurrencyCode_returnsHttp400BadRequestWithSpecificMessage(String currencyCode) throws Exception {
        // Given
        // When
        // Then
        MvcResult mvcResult = mockMvc.perform(get("/currencies/rates/{currencyCode}", currencyCode))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
        String responseBodyAsString = new String(mvcResult.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertThat(responseBodyAsString.contains(String.format("Были переданные некорректные парамтеры запроса: Валюты с кодом '%s' не существует!", currencyCode)))
                .isTrue();
    }

    @Test
    public void testGetCurrencyRateByCode_emptyCurrencyCode_returnsHttp400BadRequestWithSpecificMessage() throws Exception {
        // Given
        // When
        // Then
        MvcResult mvcResult = mockMvc.perform(get("/currencies/rates/{currencyCode}", StringUtils.SPACE))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
        String responseBodyAsString = new String(mvcResult.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertThat(responseBodyAsString.contains("Код валюты не может быть пустым!"))
                .isTrue();
    }


    @Test
    void testGetCurrencyRateByCode_validCurrencyCodeNotFoundInCentralBankResponse_returnsHttp404NotFoundWithSpecificMessage() throws Exception {
        // Given
        String validCurrencyCode = "rUb";
        when(centralBankClient.getCurrenciesExchangeRates())
                .thenReturn(new HashMap<>());
        when(currencyService.getCurrencyRateByCode(validCurrencyCode))
                .thenCallRealMethod();

        // When
        // Then
        MvcResult result = mockMvc.perform(get("/currencies/rates/{currencyCode}", validCurrencyCode))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        ExceptionsHandler.ErrorResponseMessage response = mapper.readValue(jsonResponse, ExceptionsHandler.ErrorResponseMessage.class);
        assertThat(response.message().contains(String.format("ЦБ РФ не вернул курс для валюты с кодом \"%s\".", validCurrencyCode)))
                .isTrue();
    }

    @Test
    void testConvertMoney_validCurrencyConvertRequest_returnsHttp200OkWithCorrectConvertation() throws Exception {
        // Given
        String fromCurrencyCode = "RUB";
        String toCurrencyCode = "USD";
        CurrencyConvertRequest request = new CurrencyConvertRequest(fromCurrencyCode, toCurrencyCode, BigDecimal.valueOf(100000));
        Map<String, CurrencyRate> centralBankMockResponse = Map.of(
                fromCurrencyCode, CurrencyRate.builder().code(fromCurrencyCode).exchangeRate(BigDecimal.ONE).build(),
                toCurrencyCode, CurrencyRate.builder().code(toCurrencyCode).exchangeRate(BigDecimal.valueOf(100)).build()
        );
        when(centralBankClient.getCurrenciesExchangeRates())
                .thenReturn(centralBankMockResponse);
        when(currencyService.convertMoney(request))
                .thenCallRealMethod();

        // When
        // Then
        MvcResult result = mockMvc.perform(post("/currencies/convert")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        CurrencyConvertResponse response = mapper.readValue(jsonResponse, CurrencyConvertResponse.class);

        assertThat(response.fromCurrency()).isEqualTo(fromCurrencyCode);
        assertThat(response.toCurrency()).isEqualTo(toCurrencyCode);
        assertThat(response.convertedAmount()).isEqualTo(BigDecimal.valueOf(1000).setScale(2));
    }

    @Test
    void testConvertMoney_validCurrencyCodeButNotFoundInCentralBankResponse_returnsHttp404NotFoundWithSpecificMessage() throws Exception {
        // Given
        String fromCurrencyCode = "RUB";
        String toCurrencyCode = "AED";
        CurrencyConvertRequest request = new CurrencyConvertRequest(fromCurrencyCode, toCurrencyCode, BigDecimal.valueOf(100000));
        Map<String, CurrencyRate> centralBankMockResponse = Map.of(fromCurrencyCode, CurrencyRate.builder()
                .code(fromCurrencyCode)
                .exchangeRate(BigDecimal.ONE)
                .build());
        when(centralBankClient.getCurrenciesExchangeRates())
                .thenReturn(centralBankMockResponse);
        when(currencyService.convertMoney(request))
                .thenCallRealMethod();

        // When
        // Then
        MvcResult result = mockMvc.perform(post("/currencies/convert")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        ExceptionsHandler.ErrorResponseMessage response = mapper.readValue(jsonResponse, ExceptionsHandler.ErrorResponseMessage.class);
        assertThat(response.message().contains(String.format("ЦБ РФ не вернул курс для валюты с кодом \"%s\".", toCurrencyCode)))
                .isTrue();
    }

    @Test
    void testConvertMoney_invalidCurrencyCodes_returnsHttp400BadRequestWithSpecificMessage() throws Exception {
        // Given
        String fromCurrencyCode = "invalid";
        String toCurrencyCode = "currency";
        CurrencyConvertRequest request = new CurrencyConvertRequest(fromCurrencyCode, toCurrencyCode, BigDecimal.valueOf(100000));

        // When
        // Then
        MvcResult result = mockMvc.perform(post("/currencies/convert")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        ExceptionsHandler.ErrorResponseMessage response = mapper.readValue(jsonResponse, ExceptionsHandler.ErrorResponseMessage.class);
        String errorMessageTemplate = "Валюты с кодом '%s' не существует!";
        assertThat(response.message().contains(errorMessageTemplate.formatted(fromCurrencyCode))
                && response.message().contains(errorMessageTemplate.formatted(toCurrencyCode)))
                .isTrue();
    }

    @Test
    void testConvertMoney_nonPositiveAmount_returnsHttp400BadRequestWithSpecificMessage() throws Exception {
        String fromCurrencyCode = "RUB";
        String toCurrencyCode = "AED";
        CurrencyConvertRequest request = new CurrencyConvertRequest(fromCurrencyCode, toCurrencyCode, BigDecimal.valueOf(-100000));

        // When
        // Then
        MvcResult result = mockMvc.perform(post("/currencies/convert")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        ExceptionsHandler.ErrorResponseMessage response = mapper.readValue(jsonResponse, ExceptionsHandler.ErrorResponseMessage.class);
        assertThat(response.message().contains("Сумма не может быть меньше или равна 0!"))
                .isTrue();
    }
}

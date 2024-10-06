package ru.tbank.hw8.client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.tbank.hw8.dto.CurrencyRate;
import ru.tbank.hw8.exception.IntegrationException;
import ru.tbank.hw8.exception.ServiceUnavailableException;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CentralBankClient {

    public static final String API_SERVICE_NAME = "API Центрального Банка России";
    private static final String RUB_CURRENCY_CODE = "RUB";
    private final RestClient restClient;

    @Value("${cbr-api.base-url}")
    private String baseUrl;
    @Value("${cbr-api.currency.daily-exchange-rates.path}")
    private String currencyExchangeRatesPath;
    @Value("${cbr-api.currency.daily-exchange-rates.date-param}")
    private String dateQueryParam;

    @Cacheable(value = "currencies-rates", key = "{ #root.methodName }")
    @CircuitBreaker(name = "central-bank-client", fallbackMethod = "fallbackGetCurrenciesExchangeRates")
    public Map<String, CurrencyRate> getCurrenciesExchangeRates() {
        return getCurrenciesExchangeRates(LocalDate.now());
    }

    @Cacheable(value = "currencies-rates", key = "{  #root.methodName, #date }")
    @CircuitBreaker(name = "central-bank-client", fallbackMethod = "fallbackGetCurrenciesExchangeRates")
    public Map<String, CurrencyRate> getCurrenciesExchangeRates(LocalDate date) {
        log.debug("Начало получения курсов валют на дату {} из сервиса {}.", date, API_SERVICE_NAME);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateParam = date.format(formatter);
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path(currencyExchangeRatesPath)
                    .queryParam(dateQueryParam, dateParam)
                    .build()
                    .toUri();
            List<CentralBankCurrencyRateResponse> centralBankCurrencyRateResponses = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<CentralBankCurrencyRateResponse>>() {
                    })
                    .getBody();
            if (Objects.isNull(centralBankCurrencyRateResponses)) {
                log.error("В ходе получения курсов валют на дату {} из сервиса {} пришел ответ == null!",
                        date, API_SERVICE_NAME);
                String errorMessage = String.format("В процессе получения курсов валют на дату %s, сервис ЦБ РФ вернул\s"
                        + "неожиданный результат (null).", date);
                throw new IntegrationException(errorMessage);
            }
            log.debug("Курсы валют на дату {} из сервиса {} были успешно получены! Размер полученого списка: {}.",
                    date, API_SERVICE_NAME, centralBankCurrencyRateResponses.size());
            Map<String, CurrencyRate> result = centralBankCurrencyRateResponses.stream()
                    .filter(response -> Objects.nonNull(response.getValue()))
                    .map(this::convertResponseToCurrencyRate)
                    .collect(Collectors.toMap(CurrencyRate::getCode, currencyRate -> currencyRate));
            result.put(RUB_CURRENCY_CODE, CurrencyRate.builder()
                    .code(RUB_CURRENCY_CODE)
                    .exchangeRate(BigDecimal.ONE)
                    .build());
            return result;
        } catch (Exception e) {
            log.error("В ходе получения курсов валют на дату {} из сервиса {} произошла ошибка. Причина: {}.\nStackTrace: {}",
                    date, API_SERVICE_NAME, ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
            String errorMessage = String.format("Произошла ошибка в ходе получения курсов валют на дату %s из сервиса ЦБ РФ.",
                    date);
            throw new IntegrationException(errorMessage);
        }
    }

    private Map<String, CurrencyRate> fallbackGetCurrenciesExchangeRates(Throwable throwable) throws Throwable {
        log.error("Получение курсов валют из сервиса {} невозможно. Причина: {}.\nStackTrace: {}",
                API_SERVICE_NAME, throwable.getMessage(), throwable.getStackTrace());
        String errorMessage = String.format("%s в данный момент недоступен и получение курсов валют невозможно.",
                API_SERVICE_NAME);
        if (throwable instanceof CallNotPermittedException) {
            throw new ServiceUnavailableException(errorMessage);
        } else {
            throw throwable;
        }
    }

    private CurrencyRate convertResponseToCurrencyRate(CentralBankCurrencyRateResponse response) {
        BigDecimal exchangeRate = new BigDecimal(response.getValue().replace(",", "."));
        BigDecimal unitRate = new BigDecimal(response.getUnitRate().replace(",", "."));
        return CurrencyRate.builder()
                .id(response.getId())
                .code(response.getCode())
                .numericCode(response.getNumericCode())
                .name(response.getName())
                .nominal(response.getNominal())
                .exchangeRate(exchangeRate)
                .unitRate(unitRate)
                .build();
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class CentralBankCurrencyRateResponse {
        @JacksonXmlProperty(isAttribute = true, localName = "ID")
        private String id;

        @JacksonXmlProperty(localName = "CharCode")
        private String code;

        @JacksonXmlProperty(localName = "NumCode")
        private int numericCode;

        @JacksonXmlProperty(localName = "Name")
        private String name;

        @JacksonXmlProperty(localName = "Nominal")
        private int nominal;

        @JacksonXmlProperty(localName = "Value")
        private String value;

        @JacksonXmlProperty(localName = "VunitRate")
        private String unitRate;
    }

}

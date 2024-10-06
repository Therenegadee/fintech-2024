package ru.tbank.hw8.v1.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.springframework.stereotype.Service;
import ru.tbank.hw8.client.CentralBankClient;
import ru.tbank.hw8.dto.CurrencyConvertRequest;
import ru.tbank.hw8.dto.CurrencyConvertResponse;
import ru.tbank.hw8.dto.CurrencyRate;
import ru.tbank.hw8.dto.CurrencyRateResponse;
import ru.tbank.hw8.exception.NotFoundException;
import ru.tbank.hw8.service.CurrencyService;
import ru.tbank.hw8.validation.ExistingCurrencyCode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CentralBankClient centralBankClient;

    @Override
    public CurrencyConvertResponse convertMoney(@Valid CurrencyConvertRequest request) {
        Map<String, CurrencyRate> currencyRatesByCode = new CaseInsensitiveKeyMap<>();
        currencyRatesByCode.putAll(centralBankClient.getCurrenciesExchangeRates());
        checkIfCentralBankResponseContainsRateForCurrencyCode(request.fromCurrency(), currencyRatesByCode);
        checkIfCentralBankResponseContainsRateForCurrencyCode(request.toCurrency(), currencyRatesByCode);
        CurrencyRate fromCurrency = currencyRatesByCode.get(request.fromCurrency());
        CurrencyRate toCurrency = currencyRatesByCode.get(request.toCurrency());
        log.info("Начало конвертации из валюты \"{}\" в валюту \"{}\". Сумма для конвертации: {}.",
                fromCurrency.getCode(), toCurrency.getCode(), request.amount());

        BigDecimal fromCurrencyRate = fromCurrency.getExchangeRate();
        BigDecimal toCurrencyRate = toCurrency.getExchangeRate();
        BigDecimal currencyExchangeRatio = fromCurrencyRate.divide(toCurrencyRate, MathContext.DECIMAL128);

        BigDecimal convertResult = request.amount()
                .multiply(currencyExchangeRatio)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        log.info("Результат конвертации суммы {} из валюты \"{}\" в валюту \"{}\" = {}.",
                request.amount(), fromCurrency.getCode(), toCurrency.getCode(), convertResult);
        return new CurrencyConvertResponse(fromCurrency.getCode(), toCurrency.getCode(), convertResult);
    }

    @Override
    public CurrencyRateResponse getCurrencyRateByCode(@ExistingCurrencyCode String code) {
        log.info("Начало получения курса для валюты с кодом: \"{}\".", code);
        Map<String, CurrencyRate> currencyRatesByCode = new CaseInsensitiveKeyMap<>();
        currencyRatesByCode.putAll(centralBankClient.getCurrenciesExchangeRates());
        checkIfCentralBankResponseContainsRateForCurrencyCode(code, currencyRatesByCode);
        CurrencyRate currencyRate = currencyRatesByCode.get(code);
        log.info("Курс для валюты с кодом \"{}\" был успешно получен!", code);
        return new CurrencyRateResponse(currencyRate.getCode(), currencyRate.getExchangeRate());
    }

    private void checkIfCentralBankResponseContainsRateForCurrencyCode(String currencyCode,
                                                                       Map<String, CurrencyRate> currencyRatesByCode) {
        if (!currencyRatesByCode.containsKey(currencyCode)) {
            log.warn("В ответе от сервиса {} для валюты с кодом {} не был найден курс!", CentralBankClient.API_SERVICE_NAME,
                    currencyCode);
            String errorMessage = String.format("ЦБ РФ не вернул курс для валюты с кодом \"%s\".", currencyCode);
            throw new NotFoundException(errorMessage);
        }
    }
}

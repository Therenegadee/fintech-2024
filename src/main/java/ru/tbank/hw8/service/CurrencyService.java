package ru.tbank.hw8.service;

import ru.tbank.hw8.dto.CurrencyConvertRequest;
import ru.tbank.hw8.dto.CurrencyConvertResponse;
import ru.tbank.hw8.dto.CurrencyRateResponse;

import java.math.BigDecimal;

public interface CurrencyService {

    CurrencyConvertResponse convertMoney(CurrencyConvertRequest request);

    CurrencyRateResponse getCurrencyRateByCode(String code);
}

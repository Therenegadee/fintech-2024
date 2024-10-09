package ru.tbank.hw8.dto;

import java.math.BigDecimal;

public record CurrencyRateResponse(String currency, BigDecimal rate) {
}

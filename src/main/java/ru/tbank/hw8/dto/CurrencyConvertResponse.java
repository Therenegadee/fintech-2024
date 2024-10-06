package ru.tbank.hw8.dto;

import lombok.*;

import java.math.BigDecimal;

public record CurrencyConvertResponse(String fromCurrency, String toCurrency, BigDecimal convertedAmount) { }

package ru.tbank.hw8.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRate {
    private String id;
    private String code;
    private int numericCode;
    private String name;
    private int nominal;
    private BigDecimal exchangeRate;
    private BigDecimal unitRate;
}

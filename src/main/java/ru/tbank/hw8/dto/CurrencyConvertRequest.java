package ru.tbank.hw8.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.tbank.hw8.validation.ExistingCurrencyCode;

import java.math.BigDecimal;

public record CurrencyConvertRequest(
        @NotBlank(message = "Название валюты, из которой конвертируется сумма, не может быть пустым!")
        @NotNull(message = "Название валюты, из которой конвертируется сумма, не может быть null!")
        @ExistingCurrencyCode
        @Parameter(description = "Валюта, из которой конвертируем сумму.", required = true)
        String fromCurrency,
        @NotBlank(message = "Название валюты, в которую конвертируется сумма, не может быть пустым!")
        @NotNull(message = "Название валюты, в которую конвертируется сумма, не может быть null!")
        @ExistingCurrencyCode
        @Parameter(description = "Валюта, в которую конвертируем сумму.", required = true)
        String toCurrency,
        @NotNull
        @Positive(message = "Сумма не может быть меньше или равна 0!")
        @Parameter(description = "Сумма, которую конвертируем.", required = true)
        BigDecimal amount) {
}

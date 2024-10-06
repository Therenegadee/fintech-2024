package ru.tbank.hw8.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import ru.tbank.hw8.validation.ExistingCurrencyCode;

@Getter
@Setter
@AllArgsConstructor
public class CurrencyRateRequest {

    @NotNull(message = "Код валюты не может быть null!")
    @NotEmpty(message = "Код валюты не может быть пустым!")
    @ExistingCurrencyCode
    private String code;
}

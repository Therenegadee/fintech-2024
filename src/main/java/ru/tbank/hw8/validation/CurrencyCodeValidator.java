package ru.tbank.hw8.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

import java.util.Currency;
import java.util.Objects;

@Slf4j
@Validated
public class CurrencyCodeValidator implements ConstraintValidator<ExistingCurrencyCode, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            Currency.getInstance(value.toUpperCase());
            return true;
        } catch (Exception e) {
            log.error("Валюты с кодом \"{}\" не существует!", value);
            return false;
        }
    }
}

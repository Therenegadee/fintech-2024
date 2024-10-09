package ru.tbank.hw8.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrencyCodeValidator.class)
@Target( { ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistingCurrencyCode {
    String message() default "Валюты с кодом '${validatedValue}' не существует!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

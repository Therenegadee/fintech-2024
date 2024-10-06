package ru.tbank.hw8.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.tbank.hw8.dto.CurrencyConvertRequest;
import ru.tbank.hw8.dto.CurrencyConvertResponse;
import ru.tbank.hw8.dto.CurrencyRateRequest;
import ru.tbank.hw8.dto.CurrencyRateResponse;
import ru.tbank.hw8.exception.ExceptionsHandler;
import ru.tbank.hw8.service.CurrencyService;
import ru.tbank.hw8.validation.ExistingCurrencyCode;

@RestController
@RequestMapping("/currencies")
@RequiredArgsConstructor
@Validated
@Tag(name = "v1CurrencyController")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Operation(summary = "Получение курса валюты по её коду")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrencyRateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionsHandler.ErrorResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionsHandler.ErrorResponseMessage.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionsHandler.ErrorResponseMessage.class)))
    })
    @GetMapping("/rates/{code}")
    public ResponseEntity<CurrencyRateResponse> getRateByCode(@PathVariable(name = "code")
                                                              @Parameter(name = "code", description = "Код валюты.", example = "RUB")
                                                              @NotNull(message = "Код валюты не может быть null!")
                                                              @NotBlank(message = "Код валюты не может быть пустым!")
                                                              @ExistingCurrencyCode
                                                              String code) {
        return ResponseEntity.ok(currencyService.getCurrencyRateByCode(code));
    }

    @Operation(summary = "Операция конвертации суммы из одной валюты в другую.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrencyConvertResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionsHandler.ErrorResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionsHandler.ErrorResponseMessage.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionsHandler.ErrorResponseMessage.class)))
    })
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConvertResponse> convertMoney(@RequestBody
                                                                @Valid
                                                                @io.swagger.v3.oas.annotations.parameters.RequestBody
                                                                CurrencyConvertRequest request) {
        return ResponseEntity.ok(currencyService.convertMoney(request));
    }

}

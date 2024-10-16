package ru.tbank.hw5.v1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.Event;
import ru.tbank.hw5.dto.KudaGoEventResponse;
import ru.tbank.hw5.exception.IntegrationException;
import ru.tbank.hw5.mapper.EventMapper;
import ru.tbank.hw8.dto.CurrencyConvertRequest;
import ru.tbank.hw8.service.CurrencyService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveEventService {

    private final KudaGoApiClient kudaGoApiClient;
    private final CurrencyService currencyService;
    private final EventMapper eventMapper;

    private static final Integer WEEKS_LENGTH = 7;
    private static final String RUB_CURRENCY = "rub";

    public Flux<Event> getEventsByBudgetInPeriod(double budget, String currency, LocalDate dateFrom, LocalDate dateTo) {
        log.info("Получен запрос на поиск событий по бюджету. Бюджет: {} (Валюта: {}). Дата начала событий для поиска: {}." +
                        "Дата окончания событий для поиска: {}.", budget, currency.toUpperCase(),
                Objects.nonNull(dateFrom) ? dateFrom : "отсутствует", Objects.nonNull(dateFrom) ? dateTo : "отсутствует");
        Flux<Event> events = getEventsInPeriod(dateFrom, dateTo);
        CurrencyConvertRequest request = new CurrencyConvertRequest(currency, RUB_CURRENCY, BigDecimal.valueOf(budget));
        Mono<BigDecimal> convertedMoneyMono = Mono.fromCallable(() -> currencyService.convertMoney(request).convertedAmount());

        return Mono.zip(convertedMoneyMono, events.collectList())
                .flatMapMany(tuple -> {
                    BigDecimal convertedMoney = tuple.getT1();
                    List<Event> eventList = tuple.getT2();
                    List<Event> filteredEvents = eventList.stream()
                            .filter(event -> event.getPrice().doubleValue() <= convertedMoney.doubleValue())
                            .toList();
                    log.debug("Кол-во событий после фильтра по бюджету: {}.", filteredEvents.size());
                    return Flux.fromIterable(filteredEvents);
                })
                .doFinally(signalType -> log.debug("Фильтрация событий завершена."));
    }


    public Flux<Event> getEventsInPeriod(LocalDate dateFrom, LocalDate dateTo) {
        dateFrom = Objects.isNull(dateFrom)
                ? LocalDate.now().minusDays(WEEKS_LENGTH)
                : dateFrom;
        dateTo = Objects.isNull(dateTo)
                ? LocalDate.now()
                : dateFrom;
        log.info("Начало получения событий за период {} - {}.", dateFrom, dateTo);
        long dateFromTimestamp = dateFrom.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long dateToTimestamp = dateTo.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        return kudaGoApiClient.getEventsFlux(dateFromTimestamp, dateToTimestamp)
                .switchIfEmpty(Mono.error(() -> {
                    String errorMessage = String.format("В ходе получения событий за период из сервиса %s был получен некорректный ответ (null).",
                            KudaGoApiClient.API_SERVICE_NAME);
                    log.error(errorMessage);
                    return new IntegrationException(errorMessage);
                }))
                .map(eventMapper::toEvent);
    }
}

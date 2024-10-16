package ru.tbank.hw5.v1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.tbank.hw5.client.KudaGoApiClient;
import ru.tbank.hw5.dto.Event;
import ru.tbank.hw5.dto.KudaGoEventResponse;
import ru.tbank.hw5.exception.IntegrationException;
import ru.tbank.hw5.mapper.EventMapper;
import ru.tbank.hw5.service.EventService;
import ru.tbank.hw8.dto.CurrencyConvertRequest;
import ru.tbank.hw8.service.CurrencyService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final KudaGoApiClient kudaGoApiClient;
    private final CurrencyService currencyService;
    private final EventMapper eventMapper;

    private static final Integer WEEKS_LENGTH = 7;
    private static final String RUB_CURRENCY = "rub";

    @Override
    @Async
    public CompletableFuture<List<Event>> getEventsByBudgetInPeriod(double budget, String currency, LocalDate dateFrom, LocalDate dateTo) {
        log.info("Получен запрос на поиск событий по бюджету. Бюджет: {} (Валюта: {}). Дата начала событий для поиска: {}." +
                "Дата окончания событий для поиска: {}.", budget, currency.toUpperCase(),
                Objects.nonNull(dateFrom) ? dateFrom : "отсутствует", Objects.nonNull(dateFrom) ? dateTo : "отсутствует");
        CompletableFuture<List<Event>> eventsFuture = CompletableFuture.supplyAsync(() -> getEventsInPeriod(dateFrom, dateTo));
        CompletableFuture<BigDecimal> convertedMoneyFuture = CompletableFuture.supplyAsync(
                () -> currencyService.convertMoney(new CurrencyConvertRequest(currency, RUB_CURRENCY, BigDecimal.valueOf(budget)))
                        .convertedAmount());
        return eventsFuture.thenCombine(convertedMoneyFuture, (events, convertedMoney) -> {
                    log.debug("Начало фильтрования полученных событий по Бюджету. Кол-во событий до фильтрования: {}. Бюджет" +
                            "\sв рублях: {}.", events.size(), convertedMoney);
                    events = events.stream()
                            .filter(event -> event.getPrice().doubleValue() <= (convertedMoney.doubleValue()))
                            .toList();
                    log.debug("Кол-во событий после фильтра по бюджету: {}.", events.size());
                    return events;
                });
    }

    @Override
    public List<Event> getEventsInPeriod(LocalDate dateFrom, LocalDate dateTo) {
        dateFrom = Objects.isNull(dateFrom)
                ? LocalDate.now().minusDays(WEEKS_LENGTH)
                : dateFrom;
        dateTo = Objects.isNull(dateTo)
                ? LocalDate.now()
                : dateFrom;
        log.info("Начало получения событий за период {} - {}.", dateFrom, dateTo);
        long dateFromTimestamp = dateFrom.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long dateToTimestamp = dateTo.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        List<KudaGoEventResponse.EventDTO> eventsResponse = kudaGoApiClient.getEvents(dateFromTimestamp, dateToTimestamp);
        if (Objects.isNull(eventsResponse)) {
            String errorMessage = String.format("В ходе получения событий за период из сервиса %s был получен некорректный ответ (null).",
                    KudaGoApiClient.API_SERVICE_NAME);
            log.error(errorMessage);
            throw new IntegrationException(errorMessage);
        }
        List<Event> events = eventsResponse.stream()
                .map(eventMapper::toEvent)
                .toList();
        log.info("Список полученных событий за период {} - {} содержит {} записей(-и).", dateFrom, dateTo, events.size());
        return events;
    }
}

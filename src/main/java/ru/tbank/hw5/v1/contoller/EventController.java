package ru.tbank.hw5.v1.contoller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.tbank.hw5.dto.Event;
import ru.tbank.hw5.service.EventService;
import ru.tbank.hw5.v1.service.ReactiveEventService;
import ru.tbank.hw8.validation.ExistingCurrencyCode;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final ReactiveEventService reactiveEventService;

    @GetMapping
    public ResponseEntity<List<Event>> getEvents(@RequestParam double budget,
                                                 @RequestParam @ExistingCurrencyCode String currency,
                                                 @RequestParam(required = false) LocalDate dateFrom,
                                                 @RequestParam(required = false) LocalDate dateTo) {
        return ResponseEntity.ok(eventService.getEventsByBudgetInPeriod(budget, currency, dateFrom, dateTo).join());
    }

    @GetMapping("/reactive")
    public Flux<Event> getEventsReactive(@RequestParam double budget,
                                 @RequestParam @ExistingCurrencyCode String currency,
                                 @RequestParam(required = false) LocalDate dateFrom,
                                 @RequestParam(required = false) LocalDate dateTo) {
        return reactiveEventService.getEventsByBudgetInPeriod(budget, currency, dateFrom, dateTo);
    }

}

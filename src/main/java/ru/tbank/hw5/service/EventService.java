package ru.tbank.hw5.service;

import ru.tbank.hw5.dto.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventService {

    CompletableFuture<List<Event>> getEventsByBudgetInPeriod(double budget, String currency, LocalDate dateFrom, LocalDate dateTo);

    List<Event> getEventsInPeriod(LocalDate dateFrom, LocalDate dateTo);

}

package ru.tbank.hw10.v1.service;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.hw10.dto.EventDto;
import ru.tbank.hw10.entity.Event;
import ru.tbank.hw10.entity.Place;
import ru.tbank.hw10.exception.BadRequestException;
import ru.tbank.hw10.exception.NotFoundException;
import ru.tbank.hw10.mapper.EventsMapper;
import ru.tbank.hw10.repository.EventRepository;
import ru.tbank.hw5.mapper.EventMapper;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EventsMapper eventMapper;

    public List<Event> findAllEvents() {
        List<Event> events = eventRepository.findAll();
        if (events.isEmpty()) {
            throw new NotFoundException("В Базе Данных нет никакой информации о событиях.");
        }
        return events;
    }

    public List<Event> findEventsFiltered(@Nullable String eventName, @Nullable Integer placeId, @Nullable String placeName,
                                          @Nullable OffsetDateTime fromDate, @Nullable OffsetDateTime toDate) {
        return eventRepository.findAll(EventRepository.buildSpecification(eventName, placeId, placeName, fromDate, toDate));
    }

    public Event findEventById(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Информация о событии с идентификатором " + eventId + " не была найдена."));
    }

    public Event createEvent(EventDto eventDto) {
        Event event = eventMapper.toEntity(eventDto);
        event = eventRepository.save(event);
        return event;
    }

    public void deleteEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BadRequestException("Информации о событии с идентификатором " + eventId + " не существует!"));
        eventRepository.delete(event);
    }
}

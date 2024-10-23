package ru.tbank.hw10.v1.controller;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.hw10.dto.EventDto;
import ru.tbank.hw10.mapper.EventsMapper;
import ru.tbank.hw10.v1.service.EventService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventsController {

    private final EventService eventService;
    private final EventsMapper eventMapper;

    @GetMapping("/all")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        return ResponseEntity.ok(eventService.findAllEvents().stream()
                .map(eventMapper::toDto)
                .toList());
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getAllEventsFiltered(@RequestParam(value = "eventName", required = false) @Nullable String eventName,
                                                               @RequestParam(value = "placeId", required = false) @Nullable Integer placeId,
                                                               @RequestParam(value = "placeName", required = false) @Nullable String placeName,
                                                               @RequestParam(value = "fromDate", required = false) @Nullable OffsetDateTime fromDate,
                                                               @RequestParam(value = "toDate", required = false) @Nullable OffsetDateTime toDate) {
        return ResponseEntity.ok(eventService.findEventsFiltered(eventName, placeId, placeName, fromDate, toDate).stream()
                .map(eventMapper::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable(name = "id") Integer eventId) {
        return ResponseEntity.ok(eventMapper.toDto(eventService.findEventById(eventId)));
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto dto) {
        return new ResponseEntity<>(eventMapper.toDto(eventService.createEvent(dto)), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEventById(@PathVariable(name = "id") Integer eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok().build();
    }
}

package ru.tbank.hw10.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.hw10.dto.PlaceDto;
import ru.tbank.hw10.entity.Place;
import ru.tbank.hw10.mapper.PlaceMapper;
import ru.tbank.hw10.v1.service.PlaceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceMapper placeMapper;
    
    @GetMapping("/all")
    public ResponseEntity<List<PlaceDto>> getAllPlaces() {
        return ResponseEntity.ok(placeService.findAllPlaces().stream().map(placeMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceDto> getPlaceById(@PathVariable(name = "id") Integer placeId) {
        return ResponseEntity.ok(placeMapper.toDto(placeService.findPlaceById(placeId)));
    }

    @PostMapping
    public ResponseEntity<PlaceDto> createPlace(@RequestBody PlaceDto dto) {
        return new ResponseEntity<>(placeMapper.toDto(placeService.createPlace(dto)), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlaceById(@PathVariable(name = "id") Integer placeId) {
        placeService.deletePlace(placeId);
        return ResponseEntity.ok().build();
    }
}

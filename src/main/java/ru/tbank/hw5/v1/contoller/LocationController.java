package ru.tbank.hw5.v1.contoller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.service.LocationService;

import static ru.tbank.hw5.v1.contoller.ControllerConstants.API_ROOT_PATH;

@MethodExecutionTimeTracked
@RestController
@RequestMapping(API_ROOT_PATH + "/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/{slug}")
    public ResponseEntity<Location> getLocationBySlug(@PathVariable(name = "slug") String slug) {
        return ResponseEntity.ok(locationService.findBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<String> createLocation(@RequestBody Location location) {
        locationService.save(location);
        return ResponseEntity.ok("Город был успешно создан!");
    }

    @PutMapping("/{slug}")
    public ResponseEntity<Location> updateLocationBySlug(@PathVariable(name = "slug") String slug,
                                                         @RequestBody Location location) {
        return ResponseEntity.ok(locationService.update(slug, location));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<String> deleteLocationBySlug(@PathVariable(name = "slug") String slug) {
        locationService.delete(slug);
        return ResponseEntity.ok(String.format("Город со slug \"%s\" был успешно удален!", slug));
    }
}

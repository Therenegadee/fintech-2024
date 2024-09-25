package ru.tbank.hw5.v1.contoller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.hw5.annotation.ExecutionTimeObserved;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.service.PlaceCategoryService;

import static ru.tbank.hw5.v1.contoller.ControllerConstants.API_ROOT_PATH;

@ExecutionTimeObserved
@RestController
@RequestMapping(API_ROOT_PATH + "/places/categories")
@RequiredArgsConstructor
public class PlaceCategoryController {

    private final PlaceCategoryService placeCategoryService;

    @GetMapping("/{id}")
    public ResponseEntity<PlaceCategory> getPlaceCategoryById(@PathVariable(name = "id") Integer id) {
        return ResponseEntity.ok(placeCategoryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<String> createPlaceCategory(@RequestBody PlaceCategory placeCategory) {
        placeCategoryService.save(placeCategory);
        return ResponseEntity.ok("Категория места была успешно создана!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceCategory> updatePlaceCategoryById(@PathVariable(name = "id") Integer id,
                                                                 @RequestBody PlaceCategory placeCategory) {
        return ResponseEntity.ok(placeCategoryService.update(id, placeCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePlaceCategoryById(@PathVariable(name = "id") Integer id) {
        placeCategoryService.delete(id);
        return ResponseEntity.ok(String.format("Категория места с id \"%s\" была успешно удалена!", id));
    }
}

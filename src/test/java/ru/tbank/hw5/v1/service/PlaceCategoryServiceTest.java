package ru.tbank.hw5.v1.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.hw5.cache.PlaceCategoriesCache;
import ru.tbank.hw5.dto.PlaceCategory;
import ru.tbank.hw5.exception.NotFoundException;
import ru.tbank.hw5.v1.service.PlaceCategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlaceCategoryServiceTest {
    @Mock
    private PlaceCategoriesCache placeCategoriesCache;

    @InjectMocks
    private PlaceCategoryServiceImpl placeCategoryService;

    @Test
    void testFindAll_successfully() {
        // Given
        List<PlaceCategory> preSavedPlaceCategorys = List.of(
                PlaceCategory.builder().id(1).slug("Slug-1").name("Place-Category-1").build(),
                PlaceCategory.builder().id(2).slug("Slug-2").name("Place-Category-2").build(),
                PlaceCategory.builder().id(3).slug("Slug-3").name("Place-Category-3").build()
        );
        when(placeCategoriesCache.findAll())
                .thenReturn(preSavedPlaceCategorys);

        // When
        List<PlaceCategory> placeCategories = placeCategoryService.findAll();

        // Then
        assertThat(placeCategories).hasSameElementsAs(preSavedPlaceCategorys);
    }

    @Test
    void testSaveAll_successfully() {
        // Given
        List<PlaceCategory> placeCategoriesToSave = List.of(
                PlaceCategory.builder().id(4).slug("Slug-4").name("Place-Category-4").build(),
                PlaceCategory.builder().id(5).slug("Slug-5").name("Place-Category-5").build(),
                PlaceCategory.builder().id(6).slug("Slug-6").name("Place-Category-6").build()
        );
        when(placeCategoriesCache.findAll()).thenReturn(placeCategoriesToSave);

        // When
        placeCategoryService.saveAll(placeCategoriesToSave);

        // Then
        List<PlaceCategory> placeCategoriesInCache = placeCategoriesCache.findAll();
        verify(placeCategoriesCache, times(1)).saveAll(placeCategoriesToSave);
        assertThat(placeCategoriesInCache).hasSameSizeAs(placeCategoriesToSave);
        assertThat(placeCategoriesInCache).hasSameElementsAs(placeCategoriesToSave);
    }

    @Test
    void testFindById_placeCategoryWithSuchIdExists_successfullyFetchedPlaceCategory() {
        // Given
        Integer id = 1;
        PlaceCategory expectedPlaceCategory = PlaceCategory.builder().id(id).slug("Slug-1").name("Place-Category-1").build();
        when(placeCategoriesCache.findById(eq(id))).thenReturn(Optional.of(expectedPlaceCategory));

        // When
        PlaceCategory actualPlaceCategory = placeCategoryService.findById(id);

        // Then
        assertThat(actualPlaceCategory).isEqualTo(expectedPlaceCategory);
    }

    @Test
    void testFindById_placeCategoryWithSuchIdNotExists_successfullyFetchedPlaceCategory() {
        // Given
        Integer incorrectId = 11;
        when(placeCategoriesCache.findById(eq(incorrectId))).thenReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> placeCategoryService.findById(incorrectId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.format("Категория места с идентификатором %d не была найдена!", incorrectId));
    }

    @Test
    void testSavePlaceCategory_successfully() {
        // Given
        PlaceCategory placeCategory = PlaceCategory.builder().id(1).slug("Slug-1").name("Place-Category-1").build();
        when(placeCategoriesCache.save(placeCategory)).thenReturn(placeCategory);

        // When
        placeCategoryService.save(placeCategory);

        // Then
        verify(placeCategoriesCache, times(1)).save(placeCategory);
    }

    @Test
    void testUpdate_placeCategoryWithSuchIdExists_successfully() {
        // Given
        Integer id = 1;
        PlaceCategory updatedPlaceCategory = PlaceCategory.builder().id(id).slug("Slug-1").name("Updated-Place-Category-1").build();

        // When
        placeCategoryService.update(id, updatedPlaceCategory);

        // Then
        verify(placeCategoriesCache, times(1)).update(id, updatedPlaceCategory);
    }

    @Test
    void testUpdate_placeCategoryWithSuchIdNotExists_throwIllegalArgumentException() {
        // Given
        Integer id = 1;
        PlaceCategory updatedPlaceCategory = PlaceCategory.builder().slug("Slug-1").name("Updated-Place-Category-1").build();
        when(placeCategoriesCache.update(id, updatedPlaceCategory)).thenThrow(new IllegalArgumentException());

        // When
        // Then
        assertThatThrownBy(() -> placeCategoryService.update(id, updatedPlaceCategory))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDelete_placeCategoryWithSuchIdExists_successfully() {
        // Given
        Integer idToDelete = 1;

        // When
        placeCategoryService.delete(idToDelete);

        // Then
        verify(placeCategoriesCache, times(1)).delete(idToDelete);
    }

    @Test
    void testDelete_placeCategoryWithSuchIdNotExists_throwIllegalArgumentException() {
        // Given
        Integer idToDelete = 1;
        doThrow(IllegalArgumentException.class).when(placeCategoriesCache).delete(idToDelete);

        // When
        // Then
        assertThatThrownBy(() -> placeCategoryService.delete(idToDelete))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

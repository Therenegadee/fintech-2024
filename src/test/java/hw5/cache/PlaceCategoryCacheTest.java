package hw5.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.tbank.hw5.cache.PlaceCategoriesCache;
import ru.tbank.hw5.dto.PlaceCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = PlaceCategoriesCache.class)
public class PlaceCategoryCacheTest {

    @Autowired
    private PlaceCategoriesCache placeCategoriesCache;

    private PlaceCategory placeCategory1;
    private PlaceCategory placeCategory2;
    private PlaceCategory placeCategory3;

    @BeforeEach
    public void setup() {
        placeCategoriesCache.clearCache();
        placeCategory1 = PlaceCategory.builder().name("Place-Category-1").slug("Slug-1").build();
        placeCategory2 = PlaceCategory.builder().name("Place-Category-2").slug("Slug-2").build();
        placeCategory3 = PlaceCategory.builder().name("Place-Category-3").slug("Slug-3").build();

        placeCategory1 = placeCategoriesCache.save(placeCategory1);
        placeCategory2 = placeCategoriesCache.save(placeCategory2);
        placeCategory3 = placeCategoriesCache.save(placeCategory3);
    }

    @Test
    public void testSaveAll_successfully() {
        // Given
        List<PlaceCategory> placeCategoriesToSave = List.of(
                PlaceCategory.builder().name("Place-Category-4").slug("Slug-4").build(),
                PlaceCategory.builder().name("Place-Category-5").slug("Slug-5").build(),
                PlaceCategory.builder().name("Place-Category-6").slug("Slug-6").build()
        );
        List<PlaceCategory> expectedPlaceCategories = new ArrayList<>();
        expectedPlaceCategories.addAll(List.of(placeCategory1, placeCategory2, placeCategory3));
        expectedPlaceCategories.addAll(placeCategoriesToSave);

        // When
        placeCategoriesCache.saveAll(placeCategoriesToSave);

        // Then
        List<PlaceCategory> foundPlaceCategoriesInCache = placeCategoriesCache.findAll();
        assertThat(foundPlaceCategoriesInCache).hasSameSizeAs(expectedPlaceCategories);
        assertThat(foundPlaceCategoriesInCache).hasSameElementsAs(expectedPlaceCategories);
    }

    @Test
    public void testSave_successfully() {
        // Given
        PlaceCategory placeCategoryToSave = PlaceCategory.builder().name("Place-Category-4").slug("Slug-4").build();

        // When
        placeCategoriesCache.save(placeCategoryToSave);

        // Then
        Optional<PlaceCategory> placeCategory = placeCategoriesCache.findAll()
                .stream()
                .filter(category -> category.getName().equals(placeCategoryToSave.getName())
                        && category.getSlug().equals(placeCategoryToSave.getSlug()))
                .findFirst();
        assertThat(placeCategory).isNotEmpty();
        assertThat(placeCategory).isNotNull();
    }

    @Test
    public void testFindAll_successfully() {
        // Given
        List<PlaceCategory> expectedPlaceCategories = List.of(placeCategory1, placeCategory2, placeCategory3);

        // When
        List<PlaceCategory> result = placeCategoriesCache.findAll();

        // Then
        assertThat(result).hasSameSizeAs(expectedPlaceCategories);
        assertThat(result).hasSameElementsAs(expectedPlaceCategories);
    }

    @Test
    public void testFindById_successfully() {
        // Given
        Integer seekingId = placeCategory1.getId();

        // When
        Optional<PlaceCategory> result = placeCategoriesCache.findById(seekingId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo(placeCategory1);
    }

    @Test
    public void testFindById_idNotExists_returnOptionalEmpty() {
        // Given
        Integer seekingId = 123654789;

        // When
        Optional<PlaceCategory> result = placeCategoriesCache.findById(seekingId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testUpdate_placeCategoryWithSuchIdExists_successfully() {
        // Given
        Integer idToUpdate = placeCategory1.getId();
        PlaceCategory newPlaceCategory = PlaceCategory.builder().id(idToUpdate).slug("Slug-1").name("Updated-Place-Category-1").build();

        // When
        placeCategoriesCache.update(idToUpdate, newPlaceCategory);

        // Then
        Optional<PlaceCategory> updatedPlaceCategory = placeCategoriesCache.findById(idToUpdate);
        assertThat(updatedPlaceCategory).isPresent();
        assertThat(updatedPlaceCategory).isNotNull();
        assertThat(updatedPlaceCategory.get()).isEqualTo(newPlaceCategory);
    }

    @Test
    void testUpdate_placeCategoryWithSuchIdNotExists_throwIllegalArgumentException() {
        // Given
        Integer incorrectIdToUpdate = 123654789;
        PlaceCategory newPlaceCategory = PlaceCategory.builder().id(incorrectIdToUpdate).slug("Slug-1").name("Updated-Place-Category-1").build();

        // When
        // Then
        assertThatThrownBy(() -> placeCategoriesCache.update(incorrectIdToUpdate, newPlaceCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format("Категории места с идентификатором \"%s\" не существует в кэше!", incorrectIdToUpdate));
    }

    @Test
    void testDelete_placeCategoryWithSuchIdExists_successfully() {
        // Given
        Integer idToDelete = placeCategory1.getId();

        // When
        placeCategoriesCache.delete(idToDelete);

        // Then
        Optional<PlaceCategory> deletedPlaceCategory = placeCategoriesCache.findById(idToDelete);
        assertThat(deletedPlaceCategory).isEmpty();
    }

    @Test
    void testDelete_placeCategoryWithSuchIdNotExists_throwIllegalArgumentException() {
        // Given
        Integer incorrectIdToDelete = 123654789;

        // When
        // Then
        assertThatThrownBy(() -> placeCategoriesCache.delete(incorrectIdToDelete))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format("Категории места с идентификатором \"%s\" не существует в кэше и она не может быть удалена!",
                        incorrectIdToDelete));
    }
}

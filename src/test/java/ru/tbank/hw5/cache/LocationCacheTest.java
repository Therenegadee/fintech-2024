//package ru.tbank.hw5.cache;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import ru.tbank.hw5.cache.LocationCache;
//import ru.tbank.hw5.dto.Location;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@ExtendWith(MockitoExtension.class)
//@SpringBootTest(classes = LocationCache.class)
//@Disabled
//public class LocationCacheTest {
//
//    @Autowired
//    private LocationCache locationCache;
//
//    private Location location1;
//    private Location location2;
//    private Location location3;
//
//    @BeforeEach
//    public void setup() {
//        locationCache.clearCache();
//        location1 = Location.builder().slug("Slug-1").name("Place-Category-1").build();
//        location2 = Location.builder().slug("Slug-2").name("Place-Category-2").build();
//        location3 = Location.builder().slug("Slug-3").name("Place-Category-3").build();
//
//        location1 = locationCache.save(location1);
//        location2 = locationCache.save(location2);
//        location3 = locationCache.save(location3);
//    }
//
//    @Test
//    public void testSaveAll_successfully() {
//        // Given
//        List<Location> locationsToSave = List.of(
//                Location.builder().slug("Slug-4").name("Place-Category-4").build(),
//                Location.builder().slug("Slug-5").name("Place-Category-5").build(),
//                Location.builder().slug("Slug-6").name("Place-Category-6").build()
//        );
//        List<Location> expectedPlaceCategories = new ArrayList<>();
//        expectedPlaceCategories.addAll(List.of(location1, location2, location3));
//        expectedPlaceCategories.addAll(locationsToSave);
//
//        // When
//        locationCache.saveAll(locationsToSave);
//
//        // Then
//        List<Location> foundPlaceCategoriesInCache = locationCache.findAll();
//        assertThat(foundPlaceCategoriesInCache).hasSameSizeAs(expectedPlaceCategories);
//        assertThat(foundPlaceCategoriesInCache).hasSameElementsAs(expectedPlaceCategories);
//    }
//
//    @Test
//    public void testSave_locationWithSuchSlugNotExists_successfully() {
//        // Given
//        String slug = "Slug-4";
//        Location locationToSave = Location.builder().slug(slug).name("Place-Category-4").build();
//
//        // When
//        locationCache.save(locationToSave);
//
//        // Then
//        Optional<Location> location = locationCache.findById(slug);
//        assertThat(location).isNotEmpty();
//        assertThat(location).isNotNull();
//        assertThat(location.get()).isEqualTo(locationToSave);
//    }
//
//    @Test
//    public void testSave_locationWithSuchSlugAlreadyExists_throwsIllegalArgumentException() {
//        // Given
//        String alreadyExistsSlug = location1.getSlug();
//        Location locationToSave = Location.builder().slug(alreadyExistsSlug).name("Place-Category-4").build();
//
//        // When
//        // Then
//        assertThatThrownBy(() -> locationCache.save(locationToSave))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage(String.format("Город со slug \"%s\" уже присутствует в кэше!", alreadyExistsSlug));
//
//    }
//
//    @Test
//    public void testFindAll_successfully() {
//        // Given
//        List<Location> expectedPlaceCategories = List.of(location1, location2, location3);
//
//        // When
//        List<Location> result = locationCache.findAll();
//
//        // Then
//        assertThat(result).hasSameSizeAs(expectedPlaceCategories);
//        assertThat(result).hasSameElementsAs(expectedPlaceCategories);
//    }
//
//    @Test
//    public void testFindBySlug_locationWithSuchSlugExists_successfully() {
//        // Given
//        String slug = location1.getSlug();
//
//        // When
//        Optional<Location> result = locationCache.findById(slug);
//
//        // Then
//        assertThat(result).isNotEmpty();
//        assertThat(result).isNotNull();
//        assertThat(result.get()).isEqualTo(location1);
//    }
//
//    @Test
//    public void testFindBySlug_locationWithSuchSlugNotExists_returnOptionalEmpty() {
//        // Given
//        String slug = "Incorrect-Slug";
//
//        // When
//        Optional<Location> result = locationCache.findById(slug);
//
//        // Then
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    void testUpdate_locationWithSuchSlugExists_successfully() {
//        // Given
//        String slugToUpdate = location1.getSlug();
//        Location newLocation = Location.builder().slug(slugToUpdate).name("Updated-Place-Category-1").build();
//
//        // When
//        locationCache.update(slugToUpdate, newLocation);
//
//        // Then
//        Optional<Location> updatedLocation = locationCache.findById(slugToUpdate);
//        assertThat(updatedLocation).isPresent();
//        assertThat(updatedLocation).isNotNull();
//        assertThat(updatedLocation.get()).isEqualTo(newLocation);
//    }
//
//    @Test
//    void testUpdate_locationWithSuchSlugNotExists_throwIllegalArgumentException() {
//        // Given
//        String incorrectSlugToUpdate = "Incorrect-Slug";
//        Location newLocation = Location.builder().slug(incorrectSlugToUpdate).name("Updated-Place-Category-1").build();
//
//        // When
//        // Then
//        assertThatThrownBy(() -> locationCache.update(incorrectSlugToUpdate, newLocation))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage(String.format("Города со slug \"%s\" не существует в кэше!", incorrectSlugToUpdate));
//    }
//
//    @Test
//    void testDelete_locationWithSuchSlugExists_successfully() {
//        // Given
//        String slugToDelete = location1.getSlug();
//
//        // When
//        locationCache.delete(slugToDelete);
//
//        // Then
//        Optional<Location> deletedLocation = locationCache.findById(slugToDelete);
//        assertThat(deletedLocation).isEmpty();
//    }
//
//    @Test
//    void testDelete_locationWithSuchSlugNotExists_throwIllegalArgumentException() {
//        // Given
//        String incorrectSlugToDelete = "Incorrect-Slug";
//
//        // When
//        // Then
//        assertThatThrownBy(() -> locationCache.delete(incorrectSlugToDelete))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage(String.format("Города со slug \"%s\" не существует в кэше и он не может быть удален!", incorrectSlugToDelete));
//    }
//}

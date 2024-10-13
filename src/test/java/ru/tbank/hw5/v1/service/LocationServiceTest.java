package ru.tbank.hw5.v1.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.hw5.cache.LocationCache;
import ru.tbank.hw5.dto.Location;
import ru.tbank.hw5.exception.NotFoundException;
import ru.tbank.hw5.v1.service.LocationServiceImpl;

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
public class LocationServiceTest {

    @Mock
    private LocationCache locationCache;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Test
    void testFindAll_successfully() {
        // Given
        List<Location> preSavedLocations = List.of(
                Location.builder().slug("Slug-1").name("Location-1").build(),
                Location.builder().slug("Slug-2").name("Location-2").build(),
                Location.builder().slug("Slug-3").name("Location-3").build()
        );
        when(locationCache.findAll())
                .thenReturn(preSavedLocations);

        // When
        List<Location> locations = locationService.findAll();

        // Then
        assertThat(locations).hasSameElementsAs(preSavedLocations);
    }

    @Test
    void testSaveAll_successfully() {
        // Given
        List<Location> locationsToSave = List.of(
                Location.builder().slug("Slug-4").name("Location-4").build(),
                Location.builder().slug("Slug-5").name("Location-5").build(),
                Location.builder().slug("Slug-6").name("Location-6").build()
        );
        when(locationCache.findAll()).thenReturn(locationsToSave);

        // When
        locationService.saveAll(locationsToSave);

        // Then
        List<Location> locationsInCache = locationCache.findAll();
        verify(locationCache, times(1)).saveAll(locationsToSave);
        assertThat(locationsInCache).hasSameSizeAs(locationsToSave);
        assertThat(locationsInCache).hasSameElementsAs(locationsToSave);
    }

    @Test
    void testFindBySlug_locationWithSuchSlugExists_successfullyFetchedLocation() {
        // Given
        String slug = "Slug-1";
        Location expectedLocation = Location.builder().slug(slug).name("Location-1").build();
        when(locationCache.findById(eq(slug))).thenReturn(Optional.of(expectedLocation));

        // When
        Location actualLocation = locationService.findBySlug(slug);

        // Then
        assertThat(actualLocation).isEqualTo(expectedLocation);
    }

    @Test
    void testFindBySlug_locationWithSuchSlugNotExists_successfullyFetchedLocation() {
        // Given
        String incorrectSlug = "Slug1";
        when(locationCache.findById(eq(incorrectSlug))).thenReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> locationService.findBySlug(incorrectSlug))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.format("Город со slug %s не был найден!", incorrectSlug));
    }

    @Test
    void testSaveLocation_successfully() {
        // Given
        Location location = Location.builder().slug("Slug-1").name("Location-1").build();
        when(locationCache.save(location)).thenReturn(location);

        // When
        locationService.save(location);

        // Then
        verify(locationCache, times(1)).save(location);
    }

    @Test
    void testSave_locationWithSuchSlugAlreadyExists_throwIllegalArgumentException() {
        // Given
        String slug = "Slug-1";
        Location location = Location.builder().slug(slug).name("Updated-Location-1").build();
        when(locationCache.save(location)).thenThrow(new IllegalArgumentException());

        // When
        // Then
        assertThatThrownBy(() -> locationService.save(location))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUpdate_locationWithSuchSlugExists_successfully() {
        // Given
        String slug = "Slug-1";
        Location updatedLocation = Location.builder().slug(slug).name("Updated-Location-1").build();

        // When
        locationService.update(slug, updatedLocation);

        // Then
        verify(locationCache, times(1)).update(slug, updatedLocation);
    }

    @Test
    void testUpdate_locationWithSuchSlugNotExists_throwIllegalArgumentException() {
        // Given
        String slug = "Slug-1";
        Location updatedLocation = Location.builder().slug(slug).name("Updated-Location-1").build();
        when(locationCache.update(slug, updatedLocation)).thenThrow(new IllegalArgumentException());

        // When
        // Then
        assertThatThrownBy(() -> locationService.update(slug, updatedLocation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDelete_locationWithSuchSlugExists_successfully() {
        // Given
        String slugToDelete = "Slug-1";

        // When
        locationService.delete(slugToDelete);

        // Then
        verify(locationCache, times(1)).delete(slugToDelete);
    }

    @Test
    void testDelete_locationWithSuchSlugNotExists_throwIllegalArgumentException() {
        // Given
        String slugToDelete = "Slug-1";
        doThrow(IllegalArgumentException.class).when(locationCache).delete(slugToDelete);

        // When
        // Then
        assertThatThrownBy(() -> locationService.delete(slugToDelete))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

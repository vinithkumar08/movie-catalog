package com.symphonyai.catalog.service;

import com.symphonyai.catalog.dto.DirectorRequest;
import com.symphonyai.catalog.dto.DirectorResponse;
import com.symphonyai.catalog.entity.Director;
import com.symphonyai.catalog.exception.ConflictException;
import com.symphonyai.catalog.exception.ResourceNotFoundException;
import com.symphonyai.catalog.repository.DirectorRepository;
import com.symphonyai.catalog.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectorServiceTest {

    @Mock private DirectorRepository directorRepository;
    @Mock private MovieRepository movieRepository;

    // Mockito creates DirectorService and puts the two mocks inside it
    @InjectMocks private DirectorService directorService;

    @Test
    void create_savesDirector() {
        when(directorRepository.save(any(Director.class))).thenAnswer(inv -> {
            Director d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DirectorResponse result = directorService.create(new DirectorRequest("Christopher Nolan", "British", 1970));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Christopher Nolan");
        assertThat(result.getNationality()).isEqualTo("British");
        assertThat(result.getBirthYear()).isEqualTo(1970);
    }

    @Test
    void findAll_returnsAllDirectors() {
        when(directorRepository.findAll()).thenReturn(List.of(director(1L, "A"), director(2L, "B")));

        assertThat(directorService.findAll()).hasSize(2);
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        when(directorRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directorService.findById(3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("3");
    }

    @Test
    void update_changesFields() {
        Director existing = director(1L, "Old name");
        when(directorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(directorRepository.save(existing)).thenReturn(existing);

        DirectorResponse result = directorService.update(1L, new DirectorRequest("New name", "French", 1980));

        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getNationality()).isEqualTo("French");
    }

    @Test
    void delete_removesDirector_whenTheyHaveNoMovies() {
        Director d = director(1L, "Nolan");
        when(directorRepository.findById(1L)).thenReturn(Optional.of(d));
        when(movieRepository.existsByDirectorId(1L)).thenReturn(false);

        directorService.delete(1L);

        verify(directorRepository).delete(d);
    }

    @Test
    void delete_throwsConflict_whenDirectorStillHasMovies() {
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director(1L, "Nolan")));
        when(movieRepository.existsByDirectorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> directorService.delete(1L))
                .isInstanceOf(ConflictException.class);
        verify(directorRepository, never()).delete(any());
    }

    private Director director(Long id, String name) {
        Director d = new Director();
        d.setId(id);
        d.setName(name);
        return d;
    }
}

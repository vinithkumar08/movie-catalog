package com.symphonyai.catalog.service;

import com.symphonyai.catalog.dto.GenreRequest;
import com.symphonyai.catalog.dto.GenreResponse;
import com.symphonyai.catalog.entity.Genre;
import com.symphonyai.catalog.exception.ConflictException;
import com.symphonyai.catalog.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock private GenreRepository genreRepository;
    @InjectMocks private GenreService genreService;

    @Test
    void create_savesNewGenre() {
        when(genreRepository.existsByNameIgnoreCase("Drama")).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> {
            Genre g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });

        GenreResponse result = genreService.create(new GenreRequest("Drama"));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Drama");
    }

    @Test
    void create_throwsConflict_whenNameAlreadyExists() {
        when(genreRepository.existsByNameIgnoreCase("Drama")).thenReturn(true);

        assertThatThrownBy(() -> genreService.create(new GenreRequest("Drama")))
                .isInstanceOf(ConflictException.class);
        verify(genreRepository, never()).save(any());
    }

    @Test
    void findAll_returnsAllGenres() {
        Genre action = new Genre("Action");
        action.setId(1L);
        Genre drama = new Genre("Drama");
        drama.setId(2L);
        when(genreRepository.findAll()).thenReturn(List.of(action, drama));

        List<GenreResponse> result = genreService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Action");
    }
}

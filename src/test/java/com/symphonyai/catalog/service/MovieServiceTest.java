package com.symphonyai.catalog.service;

import com.symphonyai.catalog.dto.MovieRequest;
import com.symphonyai.catalog.dto.MovieResponse;
import com.symphonyai.catalog.entity.Director;
import com.symphonyai.catalog.entity.Genre;
import com.symphonyai.catalog.entity.Movie;
import com.symphonyai.catalog.exception.BadRequestException;
import com.symphonyai.catalog.exception.ResourceNotFoundException;
import com.symphonyai.catalog.repository.DirectorRepository;
import com.symphonyai.catalog.repository.GenreRepository;
import com.symphonyai.catalog.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MovieService.
 * The repositories are Mockito "mocks" (fake objects), so no database is used here.
 */
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    private static final int MAX_GENRES = 3;
    private static final double DEFAULT_RATING = 5.0;

    @Mock private MovieRepository movieRepository;
    @Mock private DirectorRepository directorRepository;
    @Mock private GenreRepository genreRepository;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        // We pass the config values by hand, so the test does not need Spring at all.
        movieService = new MovieService(movieRepository, directorRepository, genreRepository,
                MAX_GENRES, DEFAULT_RATING);
    }

    // ---------------------------------------------------------------- create

    @Test
    void create_usesDefaultRating_whenRatingIsMissing() {
        // given
        MovieRequest request = new MovieRequest("Inception", 2010, null, 1L, Set.of(1L));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre(1L, "Sci-Fi")));
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director(1L, "Nolan")));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> {
            Movie m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        // when
        MovieResponse result = movieService.create(request);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Inception");
        assertThat(result.getRating()).isEqualTo(DEFAULT_RATING);
        assertThat(result.getDirectorName()).isEqualTo("Nolan");
        assertThat(result.getGenres()).hasSize(1);
        assertThat(result.getGenres().get(0).getName()).isEqualTo("Sci-Fi");
    }

    @Test
    void create_keepsRating_whenRatingIsSent() {
        MovieRequest request = new MovieRequest("Memento", 2000, 8.4, 1L, null);
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director(1L, "Nolan")));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        MovieResponse result = movieService.create(request);

        assertThat(result.getRating()).isEqualTo(8.4);
        assertThat(result.getGenres()).isEmpty();
    }

    @Test
    void create_throwsBadRequest_whenTooManyGenres() {
        // 4 genres, but the limit is 3
        MovieRequest request = new MovieRequest("X", 2020, 5.0, 1L, Set.of(1L, 2L, 3L, 4L));

        assertThatThrownBy(() -> movieService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("3");

        verify(movieRepository, never()).save(any());
    }

    @Test
    void create_throwsNotFound_whenDirectorDoesNotExist() {
        MovieRequest request = new MovieRequest("X", 2020, 5.0, 99L, null);
        when(directorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieRepository, never()).save(any());
    }

    @Test
    void create_throwsNotFound_whenAGenreDoesNotExist() {
        MovieRequest request = new MovieRequest("X", 2020, 5.0, 1L, Set.of(1L, 2L));
        // only genre 1 exists in the "database"
        when(genreRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(genre(1L, "Drama")));

        assertThatThrownBy(() -> movieService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(movieRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ read

    @Test
    void findById_returnsMovie() {
        Movie movie = movie(10L, "Heat", director(1L, "Mann"), genre(1L, "Crime"));
        when(movieRepository.findById(10L)).thenReturn(Optional.of(movie));

        MovieResponse result = movieService.findById(10L);

        assertThat(result.getTitle()).isEqualTo("Heat");
        assertThat(result.getGenres()).hasSize(1);
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        when(movieRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.findById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_withoutFilters_returnsEverything() {
        Director d = director(1L, "Mann");
        when(movieRepository.findAll()).thenReturn(List.of(movie(1L, "A", d), movie(2L, "B", d)));

        List<MovieResponse> result = movieService.findAll(null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_withGenreFilter_usesGenreQuery() {
        Director d = director(1L, "Mann");
        when(movieRepository.findByGenresId(1L)).thenReturn(List.of(movie(1L, "A", d)));

        List<MovieResponse> result = movieService.findAll(1L, null);

        assertThat(result).hasSize(1);
        verify(movieRepository, never()).findAll();
    }

    @Test
    void findAll_withGenreAndYear_usesCombinedQuery() {
        when(movieRepository.findByGenresIdAndReleaseYear(1L, 1995)).thenReturn(List.of());

        assertThat(movieService.findAll(1L, 1995)).isEmpty();
    }

    @Test
    void findByDirector_throwsNotFound_whenDirectorMissing() {
        when(directorRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> movieService.findByDirector(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByDirector_returnsTheirMovies() {
        Director d = director(1L, "Mann");
        when(directorRepository.existsById(1L)).thenReturn(true);
        when(movieRepository.findByDirectorId(1L)).thenReturn(List.of(movie(1L, "A", d), movie(2L, "B", d)));

        assertThat(movieService.findByDirector(1L)).hasSize(2);
    }

    // ---------------------------------------------------------------- update

    @Test
    void update_changesFields_andKeepsGenres_whenGenreIdsNotSent() {
        Movie existing = movie(10L, "Old title", director(1L, "Mann"), genre(1L, "Drama"));
        when(movieRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(directorRepository.findById(2L)).thenReturn(Optional.of(director(2L, "Nolan")));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        MovieResponse result = movieService.update(10L, new MovieRequest("New title", 2001, 7.0, 2L, null));

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getDirectorName()).isEqualTo("Nolan");
        assertThat(result.getGenres()).hasSize(1);          // unchanged
    }

    @Test
    void update_throwsNotFound_whenMovieMissing() {
        when(movieRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.update(10L, new MovieRequest("T", 2000, 5.0, 1L, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------------- delete

    @Test
    void delete_removesMovie() {
        Movie movie = movie(10L, "Heat", director(1L, "Mann"));
        when(movieRepository.findById(10L)).thenReturn(Optional.of(movie));

        movieService.delete(10L);

        verify(movieRepository).delete(movie);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(movieRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.delete(10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(movieRepository, never()).delete(any());
    }

    // ------------------------------------------------------ genre association

    @Test
    void addGenre_addsTheGenre() {
        Movie movie = movie(10L, "Heat", director(1L, "Mann"));
        when(movieRepository.findById(10L)).thenReturn(Optional.of(movie));
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre(1L, "Crime")));

        MovieResponse result = movieService.addGenre(10L, 1L);

        assertThat(result.getGenres()).hasSize(1);
    }

    @Test
    void addGenre_throwsBadRequest_whenMovieAlreadyHasMaxGenres() {
        Movie movie = movie(10L, "Heat", director(1L, "Mann"),
                genre(1L, "Crime"), genre(2L, "Action"), genre(3L, "Drama"));
        when(movieRepository.findById(10L)).thenReturn(Optional.of(movie));
        when(genreRepository.findById(4L)).thenReturn(Optional.of(genre(4L, "Thriller")));

        assertThatThrownBy(() -> movieService.addGenre(10L, 4L))
                .isInstanceOf(BadRequestException.class);
        assertThat(movie.getGenres()).hasSize(3);
    }

    @Test
    void addGenre_throwsNotFound_whenGenreMissing() {
        when(movieRepository.findById(10L)).thenReturn(Optional.of(movie(10L, "Heat", director(1L, "Mann"))));
        when(genreRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.addGenre(10L, 9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeGenre_removesTheGenre() {
        Movie movie = movie(10L, "Heat", director(1L, "Mann"), genre(1L, "Crime"), genre(2L, "Action"));
        when(movieRepository.findById(10L)).thenReturn(Optional.of(movie));

        movieService.removeGenre(10L, 1L);

        assertThat(movie.getGenres()).hasSize(1);
        assertThat(movie.getGenres().iterator().next().getName()).isEqualTo("Action");
    }

    @Test
    void removeGenre_throwsNotFound_whenGenreNotOnMovie() {
        when(movieRepository.findById(10L)).thenReturn(Optional.of(movie(10L, "Heat", director(1L, "Mann"))));

        assertThatThrownBy(() -> movieService.removeGenre(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not associated");
    }

    // ------------------------------------------------- small helper builders

    private Director director(Long id, String name) {
        Director d = new Director();
        d.setId(id);
        d.setName(name);
        return d;
    }

    private Genre genre(Long id, String name) {
        Genre g = new Genre(name);
        g.setId(id);
        return g;
    }

    private Movie movie(Long id, String title, Director director, Genre... genres) {
        Movie m = new Movie();
        m.setId(id);
        m.setTitle(title);
        m.setReleaseYear(1995);
        m.setRating(7.0);
        m.setDirector(director);
        for (Genre g : genres) {
            m.getGenres().add(g);
        }
        return m;
    }
}

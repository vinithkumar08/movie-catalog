package com.symphonyai.catalog.service;

import com.symphonyai.catalog.dto.GenreResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final GenreRepository genreRepository;

    // These two numbers come from application.properties (not hardcoded!)
    private final int maxGenresPerMovie;
    private final double defaultRating;

    public MovieService(MovieRepository movieRepository,
                        DirectorRepository directorRepository,
                        GenreRepository genreRepository,
                        @Value("${catalog.max-genres-per-movie}") int maxGenresPerMovie,
                        @Value("${catalog.default-rating}") double defaultRating) {
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
        this.genreRepository = genreRepository;
        this.maxGenresPerMovie = maxGenresPerMovie;
        this.defaultRating = defaultRating;
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------
    @Transactional
    public MovieResponse create(MovieRequest request) {
        Set<Genre> genres = loadGenres(request.getGenreIds());        // checks max genres + existence
        Director director = findDirectorOrThrow(request.getDirectorId());

        Movie movie = new Movie();
        copyFields(request, movie, director);
        movie.setGenres(genres);

        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<MovieResponse> findAll(Long genreId, Integer year) {
        List<Movie> movies;

        if (genreId != null && year != null) {
            movies = movieRepository.findByGenresIdAndReleaseYear(genreId, year);
        } else if (genreId != null) {
            movies = movieRepository.findByGenresId(genreId);
        } else if (year != null) {
            movies = movieRepository.findByReleaseYear(year);
        } else {
            movies = movieRepository.findAll();
        }

        return toResponseList(movies);
    }

    @Transactional(readOnly = true)
    public MovieResponse findById(Long id) {
        return toResponse(findMovieOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> findByDirector(Long directorId) {
        if (!directorRepository.existsById(directorId)) {
            throw new ResourceNotFoundException("Director not found with id " + directorId);
        }
        return toResponseList(movieRepository.findByDirectorId(directorId));
    }

    // ------------------------------------------------------------------
    // UPDATE
    // If genreIds is not sent, the movie keeps its current genres.
    // If genreIds is sent, it replaces the current genres.
    // ------------------------------------------------------------------
    @Transactional
    public MovieResponse update(Long id, MovieRequest request) {
        Movie movie = findMovieOrThrow(id);
        Director director = findDirectorOrThrow(request.getDirectorId());

        copyFields(request, movie, director);
        if (request.getGenreIds() != null) {
            movie.setGenres(loadGenres(request.getGenreIds()));
        }

        return toResponse(movieRepository.save(movie));
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------
    @Transactional
    public void delete(Long id) {
        Movie movie = findMovieOrThrow(id);
        movieRepository.delete(movie);
    }

    // ------------------------------------------------------------------
    // GENRE ASSOCIATION
    // ------------------------------------------------------------------
    @Transactional
    public MovieResponse addGenre(Long movieId, Long genreId) {
        Movie movie = findMovieOrThrow(movieId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id " + genreId));

        // If the movie already has this genre, there is nothing to do.
        boolean alreadyHasGenre = false;
        for (Genre g : movie.getGenres()) {
            if (g.getId().equals(genreId)) {
                alreadyHasGenre = true;
            }
        }

        if (!alreadyHasGenre) {
            if (movie.getGenres().size() >= maxGenresPerMovie) {
                throw new BadRequestException("A movie can have at most " + maxGenresPerMovie + " genres");
            }
            movie.getGenres().add(genre);
        }
        return toResponse(movie);
    }

    @Transactional
    public void removeGenre(Long movieId, Long genreId) {
        Movie movie = findMovieOrThrow(movieId);

        // removeIf deletes every genre that matches and tells us if it removed anything
        boolean removed = movie.getGenres().removeIf(g -> g.getId().equals(genreId));
        if (!removed) {
            throw new ResourceNotFoundException(
                    "Genre " + genreId + " is not associated with movie " + movieId);
        }
    }

    // ==================================================================
    // Private helper methods
    // ==================================================================

    private Movie findMovieOrThrow(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id " + id));
    }

    private Director findDirectorOrThrow(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id " + id));
    }

    /** Copies title, year, rating and director from the request into the movie. */
    private void copyFields(MovieRequest request, Movie movie, Director director) {
        movie.setTitle(request.getTitle().trim());
        movie.setReleaseYear(request.getReleaseYear());
        movie.setDirector(director);

        // If no rating was sent, use the default from application.properties
        if (request.getRating() != null) {
            movie.setRating(request.getRating());
        } else {
            movie.setRating(defaultRating);
        }
    }

    /**
     * Turns a list of genre ids into Genre entities.
     * - null / empty list  -> no genres
     * - too many ids       -> 400 (limit comes from application.properties)
     * - an unknown id      -> 404
     */
    private Set<Genre> loadGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        if (genreIds.size() > maxGenresPerMovie) {
            throw new BadRequestException("A movie can have at most " + maxGenresPerMovie + " genres");
        }

        List<Genre> found = genreRepository.findAllById(genreIds);
        if (found.size() != genreIds.size()) {
            throw new ResourceNotFoundException("One or more genres were not found: " + genreIds);
        }
        return new HashSet<>(found);
    }

    /** Converts a Movie entity into the JSON object we send back. */
    private MovieResponse toResponse(Movie movie) {
        List<GenreResponse> genres = new ArrayList<>();
        for (Genre genre : movie.getGenres()) {
            genres.add(new GenreResponse(genre.getId(), genre.getName()));
        }
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getReleaseYear(),
                movie.getRating(),
                movie.getDirector().getId(),
                movie.getDirector().getName(),
                genres);
    }

    private List<MovieResponse> toResponseList(List<Movie> movies) {
        List<MovieResponse> result = new ArrayList<>();
        for (Movie movie : movies) {
            result.add(toResponse(movie));
        }
        return result;
    }
}

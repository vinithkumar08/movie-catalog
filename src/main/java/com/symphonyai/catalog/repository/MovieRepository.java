package com.symphonyai.catalog.repository;

import com.symphonyai.catalog.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Is there any movie whose director has this id?
    boolean existsByDirectorId(Long directorId);

    // All movies of one director
    List<Movie> findByDirectorId(Long directorId);

    // Filters for GET /api/movies  (the name is read as: genres.id / releaseYear)
    List<Movie> findByGenresId(Long genreId);
    List<Movie> findByReleaseYear(Integer year);
    List<Movie> findByGenresIdAndReleaseYear(Long genreId, Integer year);
}

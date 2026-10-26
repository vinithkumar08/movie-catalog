package com.symphonyai.catalog.controller;

import com.symphonyai.catalog.dto.MovieRequest;
import com.symphonyai.catalog.dto.MovieResponse;
import com.symphonyai.catalog.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse create(@Valid @RequestBody MovieRequest request) {
        return movieService.create(request);
    }

    // Optional filters:  /api/movies?genreId=1&year=2010
    @GetMapping
    public List<MovieResponse> getAll(@RequestParam(required = false) Long genreId,
                                      @RequestParam(required = false) Integer year) {
        return movieService.findAll(genreId, year);
    }

    @GetMapping("/{id}")
    public MovieResponse getById(@PathVariable Long id) {
        return movieService.findById(id);
    }

    @PutMapping("/{id}")
    public MovieResponse update(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        return movieService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        movieService.delete(id);
    }

    @PostMapping("/{id}/genres/{gid}")
    public MovieResponse addGenre(@PathVariable Long id, @PathVariable("gid") Long genreId) {
        return movieService.addGenre(id, genreId);
    }

    @DeleteMapping("/{id}/genres/{gid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGenre(@PathVariable Long id, @PathVariable("gid") Long genreId) {
        movieService.removeGenre(id, genreId);
    }
}

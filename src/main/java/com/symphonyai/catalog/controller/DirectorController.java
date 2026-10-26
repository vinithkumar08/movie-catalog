package com.symphonyai.catalog.controller;

import com.symphonyai.catalog.dto.DirectorRequest;
import com.symphonyai.catalog.dto.DirectorResponse;
import com.symphonyai.catalog.dto.MovieResponse;
import com.symphonyai.catalog.service.DirectorService;
import com.symphonyai.catalog.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directors")
public class DirectorController {

    private final DirectorService directorService;
    private final MovieService movieService;

    public DirectorController(DirectorService directorService, MovieService movieService) {
        this.directorService = directorService;
        this.movieService = movieService;
    }

    // @Valid -> runs the @NotBlank etc. checks on the request body
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)          // 201
    public DirectorResponse create(@Valid @RequestBody DirectorRequest request) {
        return directorService.create(request);
    }

    @GetMapping
    public List<DirectorResponse> getAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public DirectorResponse getById(@PathVariable Long id) {
        return directorService.findById(id);
    }

    @PutMapping("/{id}")
    public DirectorResponse update(@PathVariable Long id, @Valid @RequestBody DirectorRequest request) {
        return directorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)       // 204
    public void delete(@PathVariable Long id) {
        directorService.delete(id);
    }

    @GetMapping("/{id}/movies")
    public List<MovieResponse> getMoviesOfDirector(@PathVariable Long id) {
        return movieService.findByDirector(id);
    }
}

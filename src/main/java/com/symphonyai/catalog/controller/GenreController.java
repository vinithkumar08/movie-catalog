package com.symphonyai.catalog.controller;

import com.symphonyai.catalog.dto.GenreRequest;
import com.symphonyai.catalog.dto.GenreResponse;
import com.symphonyai.catalog.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenreResponse create(@Valid @RequestBody GenreRequest request) {
        return genreService.create(request);
    }

    @GetMapping
    public List<GenreResponse> getAll() {
        return genreService.findAll();
    }
}

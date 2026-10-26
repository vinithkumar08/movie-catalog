package com.symphonyai.catalog.service;

import com.symphonyai.catalog.dto.GenreRequest;
import com.symphonyai.catalog.dto.GenreResponse;
import com.symphonyai.catalog.entity.Genre;
import com.symphonyai.catalog.exception.ConflictException;
import com.symphonyai.catalog.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class GenreService {

    private final GenreRepository genreRepository;

    // Spring "injects" the repository here automatically (constructor injection).
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional
    public GenreResponse create(GenreRequest request) {
        String name = request.getName().trim();

        // Genre names must be unique
        if (genreRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Genre already exists: " + name);
        }

        Genre saved = genreRepository.save(new Genre(name));
        return new GenreResponse(saved.getId(), saved.getName());
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> findAll() {
        List<GenreResponse> result = new ArrayList<>();
        for (Genre genre : genreRepository.findAll()) {
            result.add(new GenreResponse(genre.getId(), genre.getName()));
        }
        return result;
    }
}

package com.symphonyai.catalog.service;

import com.symphonyai.catalog.dto.DirectorRequest;
import com.symphonyai.catalog.dto.DirectorResponse;
import com.symphonyai.catalog.entity.Director;
import com.symphonyai.catalog.exception.ConflictException;
import com.symphonyai.catalog.exception.ResourceNotFoundException;
import com.symphonyai.catalog.repository.DirectorRepository;
import com.symphonyai.catalog.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;

    public DirectorService(DirectorRepository directorRepository, MovieRepository movieRepository) {
        this.directorRepository = directorRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional
    public DirectorResponse create(DirectorRequest request) {
        Director director = new Director();
        copyFields(request, director);
        return toResponse(directorRepository.save(director));
    }

    @Transactional(readOnly = true)
    public List<DirectorResponse> findAll() {
        List<DirectorResponse> result = new ArrayList<>();
        for (Director director : directorRepository.findAll()) {
            result.add(toResponse(director));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DirectorResponse findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public DirectorResponse update(Long id, DirectorRequest request) {
        Director director = findOrThrow(id);      // 404 if it does not exist
        copyFields(request, director);
        return toResponse(directorRepository.save(director));
    }

    @Transactional
    public void delete(Long id) {
        Director director = findOrThrow(id);

        // A movie must always have a director, so we refuse to delete a director who still has movies.
        if (movieRepository.existsByDirectorId(id)) {
            throw new ConflictException("Cannot delete director " + id + " because they still have movies");
        }
        directorRepository.delete(director);
    }

    // ---------- helper methods ----------

    private Director findOrThrow(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id " + id));
    }

    private void copyFields(DirectorRequest request, Director director) {
        director.setName(request.getName().trim());
        director.setNationality(request.getNationality());
        director.setBirthYear(request.getBirthYear());
    }

    private DirectorResponse toResponse(Director d) {
        return new DirectorResponse(d.getId(), d.getName(), d.getNationality(), d.getBirthYear());
    }
}

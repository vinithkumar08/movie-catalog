package com.symphonyai.catalog.repository;

import com.symphonyai.catalog.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    // SELECT count(*) > 0 FROM genre WHERE lower(name) = lower(?)
    boolean existsByNameIgnoreCase(String name);
}

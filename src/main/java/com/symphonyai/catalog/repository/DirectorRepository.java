package com.symphonyai.catalog.repository;

import com.symphonyai.catalog.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;

/** Gives us save, findById, findAll, deleteById ... for free. */
public interface DirectorRepository extends JpaRepository<Director, Long> {
}

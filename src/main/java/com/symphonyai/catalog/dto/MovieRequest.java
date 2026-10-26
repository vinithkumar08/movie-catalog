package com.symphonyai.catalog.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/** JSON body for creating / updating a movie. */
public class MovieRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotNull(message = "releaseYear is required")
    private Integer releaseYear;

    // optional; if missing we use catalog.default-rating
    @DecimalMin(value = "0.0", message = "rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "rating must be at most 10.0")
    private Double rating;

    @NotNull(message = "directorId is required")
    private Long directorId;

    // optional list of genre ids, e.g. [1, 2]
    private Set<Long> genreIds;

    public MovieRequest() {
    }

    public MovieRequest(String title, Integer releaseYear, Double rating, Long directorId, Set<Long> genreIds) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.directorId = directorId;
        this.genreIds = genreIds;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Long getDirectorId() { return directorId; }
    public void setDirectorId(Long directorId) { this.directorId = directorId; }
    public Set<Long> getGenreIds() { return genreIds; }
    public void setGenreIds(Set<Long> genreIds) { this.genreIds = genreIds; }
}

package com.symphonyai.catalog.dto;

import java.util.List;

/** JSON we return for a movie, including its director name and genres. */
public class MovieResponse {

    private Long id;
    private String title;
    private Integer releaseYear;
    private Double rating;
    private Long directorId;
    private String directorName;
    private List<GenreResponse> genres;

    public MovieResponse(Long id, String title, Integer releaseYear, Double rating,
                         Long directorId, String directorName, List<GenreResponse> genres) {
        this.id = id;
        this.title = title;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.directorId = directorId;
        this.directorName = directorName;
        this.genres = genres;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Integer getReleaseYear() { return releaseYear; }
    public Double getRating() { return rating; }
    public Long getDirectorId() { return directorId; }
    public String getDirectorName() { return directorName; }
    public List<GenreResponse> getGenres() { return genres; }
}

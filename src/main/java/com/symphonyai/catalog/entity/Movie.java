package com.symphonyai.catalog.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/** Table: movie. Each movie has exactly one director and many genres. */
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer releaseYear;

    private Double rating;   // 0.0 - 10.0, may be null

    // Many movies -> one director. This creates the column "director_id" (foreign key).
    @ManyToOne(optional = false)
    @JoinColumn(name = "director_id")
    private Director director;

    // Many movies <-> many genres. Hibernate creates the join table "movie_genre"
    // with two columns: movie_id and genre_id.
    @ManyToMany
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Director getDirector() { return director; }
    public void setDirector(Director director) { this.director = director; }
    public Set<Genre> getGenres() { return genres; }
    public void setGenres(Set<Genre> genres) { this.genres = genres; }
}

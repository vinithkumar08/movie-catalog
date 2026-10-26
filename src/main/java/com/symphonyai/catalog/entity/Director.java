package com.symphonyai.catalog.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Table: director. One director can have many movies. */
@Entity
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // database generates the id
    private Long id;

    @Column(nullable = false)
    private String name;

    private String nationality;   // optional
    private Integer birthYear;    // optional

    // "mappedBy" means: the foreign key lives in the Movie table (Movie.director owns it).
    @OneToMany(mappedBy = "director")
    private List<Movie> movies = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }
    public List<Movie> getMovies() { return movies; }
}

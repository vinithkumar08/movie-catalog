package com.symphonyai.catalog.dto;

/** JSON we return for a director. */
public class DirectorResponse {

    private Long id;
    private String name;
    private String nationality;
    private Integer birthYear;

    public DirectorResponse(Long id, String name, String nationality, Integer birthYear) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.birthYear = birthYear;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public Integer getBirthYear() { return birthYear; }
}

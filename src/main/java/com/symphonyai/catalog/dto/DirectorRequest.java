package com.symphonyai.catalog.dto;

import jakarta.validation.constraints.NotBlank;

/** JSON body for creating / updating a director. */
public class DirectorRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String nationality;
    private Integer birthYear;

    public DirectorRequest() {
    }

    public DirectorRequest(String name, String nationality, Integer birthYear) {
        this.name = name;
        this.nationality = nationality;
        this.birthYear = birthYear;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }
}

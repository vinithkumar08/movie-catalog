package com.symphonyai.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public class GenreRequest {

    @NotBlank(message = "name is required")
    private String name;

    public GenreRequest() {
    }

    public GenreRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

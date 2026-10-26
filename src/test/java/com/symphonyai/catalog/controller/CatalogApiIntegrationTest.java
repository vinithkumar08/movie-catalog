package com.symphonyai.catalog.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests: the whole app starts (with H2) and we call the real endpoints with MockMvc.
 * @Transactional makes every test roll back its data, so tests don't affect each other.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "catalog.max-genres-per-movie=2")
class CatalogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void infoEndpoint_returnsValuesFromProperties() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Movie Catalog Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    void createDirectorGenreAndMovie_thenReadThemBack() throws Exception {
        long directorId = createAndGetId("/api/directors",
                "{\"name\":\"Christopher Nolan\",\"nationality\":\"British\",\"birthYear\":1970}");
        long genreId = createAndGetId("/api/genres", "{\"name\":\"Sci-Fi\"}");
        long movieId = createAndGetId("/api/movies",
                "{\"title\":\"Inception\",\"releaseYear\":2010,\"directorId\":" + directorId
                        + ",\"genreIds\":[" + genreId + "]}");

        // read one movie: genres included, rating = default from application.properties
        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.rating").value(5.0))
                .andExpect(jsonPath("$.directorName").value("Christopher Nolan"))
                .andExpect(jsonPath("$.genres[0].name").value("Sci-Fi"));

        // movies by director
        mockMvc.perform(get("/api/directors/" + directorId + "/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // filters
        mockMvc.perform(get("/api/movies?year=2010&genreId=" + genreId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/movies?year=1999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addAndRemoveGenreOnMovie() throws Exception {
        long directorId = createAndGetId("/api/directors", "{\"name\":\"Denis Villeneuve\"}");
        long genreId = createAndGetId("/api/genres", "{\"name\":\"Drama\"}");
        long movieId = createAndGetId("/api/movies",
                "{\"title\":\"Arrival\",\"releaseYear\":2016,\"directorId\":" + directorId + "}");

        mockMvc.perform(post("/api/movies/" + movieId + "/genres/" + genreId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genres", hasSize(1)));

        mockMvc.perform(delete("/api/movies/" + movieId + "/genres/" + genreId))
                .andExpect(status().isNoContent());

        // removing it a second time -> 404
        mockMvc.perform(delete("/api/movies/" + movieId + "/genres/" + genreId))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidMovie_returns400_withFieldErrors() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"rating\":11.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.releaseYear").exists())
                .andExpect(jsonPath("$.fieldErrors.directorId").exists())
                .andExpect(jsonPath("$.fieldErrors.rating").exists());
    }

    @Test
    void unknownMovie_returns404() throws Exception {
        mockMvc.perform(get("/api/movies/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Movie not found with id 999999"));
    }

    @Test
    void duplicateGenre_returns409() throws Exception {
        createAndGetId("/api/genres", "{\"name\":\"Horror\"}");

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"horror\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteDirectorWithMovies_returns409() throws Exception {
        long directorId = createAndGetId("/api/directors", "{\"name\":\"Greta Gerwig\"}");
        createAndGetId("/api/movies",
                "{\"title\":\"Lady Bird\",\"releaseYear\":2017,\"directorId\":" + directorId + "}");

        mockMvc.perform(delete("/api/directors/" + directorId))
                .andExpect(status().isConflict());
    }

    @Test
    void tooManyGenres_returns400() throws Exception {
        long directorId = createAndGetId("/api/directors", "{\"name\":\"Someone\"}");
        long g1 = createAndGetId("/api/genres", "{\"name\":\"G1\"}");
        long g2 = createAndGetId("/api/genres", "{\"name\":\"G2\"}");
        long g3 = createAndGetId("/api/genres", "{\"name\":\"G3\"}");

        // the limit is 2 in this test class (see @TestPropertySource above)
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Too many\",\"releaseYear\":2020,\"directorId\":" + directorId
                                + ",\"genreIds\":[" + g1 + "," + g2 + "," + g3 + "]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A movie can have at most 2 genres"));
    }

    /** Sends a POST, checks it returned 201, and gives back the new id from the JSON. */
    private long createAndGetId(String url, String json) throws Exception {
        String responseBody = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(responseBody, "$.id");
        return id.longValue();
    }
}

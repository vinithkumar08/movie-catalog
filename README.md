# Movie Catalog Service (simple version)

A Spring Boot REST API to manage **directors**, **movies** and **genres**.
This version is written to be easy to read: plain classes, simple loops, and comments that explain what each part does.

- A **Director** has many **Movies**
- A **Movie** has exactly one Director
- A **Movie** has many **Genres**, and a Genre can be on many Movies (join table `movie_genre`)

## Requirements
- Java 17+
- Maven 3.9+

## How to run
```bash
mvn spring-boot:run
```
The app runs at http://localhost:8080

| URL | What you get |
|---|---|
| http://localhost:8080/swagger-ui.html | Swagger UI to try the API in the browser |
| http://localhost:8080/h2-console | Database viewer. JDBC URL: `jdbc:h2:mem:catalogdb`, user: `sa`, password: empty |

Run the tests:
```bash
mvn test
```

Docker (optional):
```bash
docker build -t movie-catalog-simple .
docker run -p 8080:8080 movie-catalog-simple
```

## Settings (src/main/resources/application.properties)
| Property | Value | Meaning |
|---|---|---|
| `app.name` | Movie Catalog Service | shown by `GET /api/info` |
| `app.version` | 1.0.0 | shown by `GET /api/info` |
| `catalog.max-genres-per-movie` | 5 | a movie cannot have more genres than this |
| `catalog.default-rating` | 5.0 | rating used when a movie is created without one |

The code reads them with `@Value("${...}")`, so they are never hardcoded.

## Project structure (and what each part does)
```
controller/   receives HTTP requests, calls a service, returns JSON
service/      business rules (validation of genres, default rating, 404 checks ...)
repository/   talks to the database (Spring writes the SQL for us)
entity/       Java classes that become database tables
dto/          the JSON shapes we receive (Request) and send (Response)
exception/    our error classes + one class that turns them into JSON errors
```

### What happens on `POST /api/movies`
1. **MovieController** receives the JSON and converts it to a `MovieRequest`. `@Valid` checks the rules (title not blank ...).
2. **MovieService.create** checks the genre limit, loads the director and genres from the database, applies the default rating if needed, and saves the `Movie`.
3. **MovieRepository.save** inserts the row (and the rows in `movie_genre`).
4. The service converts the saved `Movie` into a `MovieResponse` and the controller returns it with status `201`.
5. If something goes wrong, the service throws an exception and **GlobalExceptionHandler** returns a JSON error with the right status.

## Error responses
All errors look like this:
```json
{
  "timestamp": "2025-01-15T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/movies",
  "fieldErrors": { "title": "title is required" }
}
```
| Status | When |
|---|---|
| 400 | invalid input, broken JSON, or too many genres on a movie |
| 404 | director / movie / genre not found |
| 409 | genre name already exists, or deleting a director that still has movies |

## Sample curl requests
```bash
# app info
curl localhost:8080/api/info

# create a director
curl -X POST localhost:8080/api/directors -H "Content-Type: application/json" \
  -d '{"name":"Christopher Nolan","nationality":"British","birthYear":1970}'

# create genres
curl -X POST localhost:8080/api/genres -H "Content-Type: application/json" -d '{"name":"Sci-Fi"}'
curl -X POST localhost:8080/api/genres -H "Content-Type: application/json" -d '{"name":"Thriller"}'

# create a movie (no rating sent -> default rating is used)
curl -X POST localhost:8080/api/movies -H "Content-Type: application/json" \
  -d '{"title":"Inception","releaseYear":2010,"directorId":1,"genreIds":[1,2]}'

# read
curl localhost:8080/api/directors
curl localhost:8080/api/directors/1
curl localhost:8080/api/directors/1/movies
curl localhost:8080/api/genres
curl localhost:8080/api/movies
curl "localhost:8080/api/movies?genreId=1&year=2010"
curl localhost:8080/api/movies/1

# update
curl -X PUT localhost:8080/api/directors/1 -H "Content-Type: application/json" \
  -d '{"name":"Christopher Nolan","nationality":"British-American","birthYear":1970}'
curl -X PUT localhost:8080/api/movies/1 -H "Content-Type: application/json" \
  -d '{"title":"Inception","releaseYear":2010,"rating":9.0,"directorId":1}'

# add / remove a genre on a movie
curl -X POST   localhost:8080/api/movies/1/genres/2
curl -X DELETE localhost:8080/api/movies/1/genres/2

# delete (delete the movie before its director)
curl -X DELETE localhost:8080/api/movies/1
curl -X DELETE localhost:8080/api/directors/1
```

## Rules worth knowing
- `PUT /api/movies/{id}`: if `genreIds` is left out, the movie keeps its genres. If it is sent, it replaces them.
- Adding a genre that the movie already has does nothing (no error).
- A director with movies cannot be deleted (409).

## API contract
The full OpenAPI 3.0 description is in [`openapi.yaml`](openapi.yaml) in the project root.

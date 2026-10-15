# Spring Batch CSV Import

A small Spring Boot application that demonstrates a chunk-oriented Spring Batch job:

```text
CSV -> FlatFileItemReader -> UserProcessor -> UserWriter
```

The processor filters rows with an invalid email or ZIP code and uppercases valid users' first names. The writer persists every accepted row to the PostgreSQL `user_records` table through a Spring Data JPA repository. The job uses chunks of 10 records, and Spring Batch stores its execution metadata in PostgreSQL. Tests use an isolated in-memory H2 database.

## Requirements

- Java 21
- Maven 3.9+, or use the included Maven wrapper
- PostgreSQL with a `weather-sec` database

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

The application exposes `POST /api/imports`. Upload the CSV as multipart form data; the server stores it in the configured upload directory and passes the safe server-side path to a step-scoped reader.

```powershell
curl.exe -F "file=@src/main/resources/user_details_500.csv" http://localhost:8080/api/imports
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html`; the raw OpenAPI definition is at `http://localhost:8080/v3/api-docs`.

Spring Batch automatically creates and uses its `BATCH_*` repository tables in PostgreSQL. `schema.sql` creates the `user_records` business table if it does not exist. Configure the connection in `src/main/resources/application.properties` before running in another environment.

## Test

```powershell
.\mvnw.cmd test
```

To import another classpath CSV, change `input.file` in `application.properties`. The expected header is:

```text
firstName,lastName,zipcode,phone1,phone2,email,web
```

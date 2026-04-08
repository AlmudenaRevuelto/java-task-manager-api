# Task Manager API

REST API for task management built with Java and Spring Boot. Supports full CRUD operations with validation, error handling, Swagger documentation and Docker support.

## Tech Stack

- **Java 21** — LTS runtime
- **Spring Boot 4.0** — web, JPA, validation, devtools
- **PostgreSQL** — production database
- **H2** — in-memory database for tests
- **springdoc-openapi 3** — Swagger UI
- **Maven Wrapper** — reproducible builds

---

## Requirements

- Java 21+
- Maven 3.9+ (or use the included `./mvnw`)
- Docker (optional, for running with PostgreSQL)

---

## Run locally

### With PostgreSQL (Docker)

```bash
docker-compose up --build
```

App will be available at `http://localhost:8080`.

### Without Docker

Start a PostgreSQL instance and set the following environment variables (or edit `application.properties`):

| Variable  | Default     | Description          |
|-----------|-------------|----------------------|
| `DB_HOST` | `localhost` | PostgreSQL host      |
| `DB_PORT` | `5432`      | PostgreSQL port      |
| `DB_NAME` | `taskdb`    | Database name        |
| `DB_USER` | `postgres`  | Database user        |
| `DB_PASS` | `postgres`  | Database password    |

Then run:

```bash
./mvnw spring-boot:run
```

---

## Run tests

Tests run against an in-memory H2 database — no external services required.

```bash
./mvnw test
```

---

## API Endpoints

| Method   | Endpoint       | Description              | Status codes   |
|----------|----------------|--------------------------|----------------|
| `GET`    | `/tasks`       | List all tasks           | 200            |
| `GET`    | `/tasks/{id}`  | Get a task by ID         | 200, 404       |
| `POST`   | `/tasks`       | Create a new task        | 201, 400       |
| `PUT`    | `/tasks/{id}`  | Update a task by ID      | 200, 400, 404  |
| `DELETE` | `/tasks/{id}`  | Delete a task by ID      | 204, 404       |

### Task fields

| Field         | Type      | Required | Description                              |
|---------------|-----------|----------|------------------------------------------|
| `title`       | `string`  | Yes      | Task title                               |
| `description` | `string`  | No       | Optional details                         |
| `completed`   | `boolean` | No       | Completion status. Defaults to `false`   |
| `priority`    | `enum`    | No       | `LOW`, `MEDIUM`, `HIGH`. Defaults to `MEDIUM` |
| `dueDate`     | `date`    | No       | Due date in `YYYY-MM-DD` format          |

### Example request

```json
POST /tasks
{
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "completed": false,
  "priority": "HIGH",
  "dueDate": "2026-12-31"
}
```

---

## Swagger UI

Interactive API documentation available at:

```
http://localhost:8080/swagger-ui.html
```

---

## Project Structure

```
src/
├── main/java/taskmanager/
│   ├── config/         # OpenAPI configuration
│   ├── controller/     # REST endpoints
│   ├── dto/            # Request and response DTOs
│   ├── exception/      # Global error handling
│   ├── model/          # JPA entities
│   ├── repository/     # Spring Data repositories
│   └── service/        # Business logic
└── test/
    └── java/taskmanager/
        └── controller/ # Integration tests (MockMvc + H2)
```

---

## License

[MIT](LICENSE)

# Task Manager API

REST API for task management built with Java and Spring Boot. Supports full CRUD operations with filtering, pagination, sorting, free-text search, JWT authentication, validation, error handling, Swagger documentation and Docker support.

## Tech Stack

- **Java 17** — LTS runtime
- **Spring Boot 4.0** — web, JPA, validation, security, devtools
- **Spring Security** — JWT Bearer + HTTP Basic authentication with BCrypt password encoding
- **JJWT 0.11.5** — JWT token generation and validation
- **PostgreSQL** — production database
- **H2** — in-memory database for tests
- **springdoc-openapi 3** — Swagger UI with Basic Auth and Bearer token support
- **Maven Wrapper** — reproducible builds

---

## Requirements

- Java 17+
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

| Variable     | Default                               | Description                        |
|--------------|---------------------------------------|------------------------------------|
| `DB_HOST`    | `localhost`                           | PostgreSQL host                    |
| `DB_PORT`    | `5432`                                | PostgreSQL port                    |
| `DB_NAME`    | `taskdb`                              | Database name                      |
| `DB_USER`    | `postgres`                            | Database user                      |
| `DB_PASS`    | `postgres`                            | Database password                  |
| `JWT_SECRET` | `change-me-in-production-min-32-chars!!` | JWT signing secret (min 32 chars) |

Then run:

```bash
./mvnw spring-boot:run
```

> **Tip:** If port 8080 is already in use, kill the previous process first:
> ```bash
> fuser -k 8080/tcp; ./mvnw spring-boot:run
> ```

---

## Authentication

The API supports two authentication mechanisms:

- **JWT Bearer** — recommended. Obtain a token from `POST /auth/login` and send it as `Authorization: Bearer <token>`.
- **HTTP Basic** — username and password on every request (kept for Swagger UI convenience).

Public endpoints (no authentication required): `POST /auth/register`, `POST /auth/login`, Swagger UI and OpenAPI docs.

### Register and login

```
POST /auth/register   →  { "token": "eyJhbGci...", "username": "admin" }
POST /auth/login      →  { "token": "eyJhbGci...", "username": "admin" }
```

Use the returned token in all subsequent requests:

```
Authorization: Bearer eyJhbGci...
```

Tokens expire after **24 hours** (configurable via `app.jwt.expiration-ms`).

### Create users manually (PostgreSQL)

```sql
INSERT INTO users (username, password)
VALUES ('admin', '$2a$10$<bcrypt_hash>');
```

Generate a BCrypt hash:

```bash
htpasswd -bnBC 10 "" yourpassword | tr -d ':\n'
```

---

## Run tests

Tests run against an in-memory H2 database — no external services or authentication required. Security is disabled in the `test` profile.

```bash
./mvnw test
```

SQL statements executed during tests are printed to the console (`spring.jpa.show-sql=true` in the test profile).

---

## API Endpoints

### Auth

| Method | Endpoint          | Description                          | Status codes |
|--------|-------------------|--------------------------------------|--------------|
| `POST` | `/auth/register`  | Register a new user, returns JWT     | 201, 400     |
| `POST` | `/auth/login`     | Authenticate and return JWT          | 200, 401     |

### Tasks

| Method   | Endpoint       | Description              | Auth required | Status codes   |
|----------|----------------|--------------------------|---------------|----------------|
| `GET`    | `/tasks`       | List / filter tasks      | Yes           | 200            |
| `GET`    | `/tasks/{id}`  | Get a task by ID         | Yes           | 200, 404       |
| `POST`   | `/tasks`       | Create a new task        | Yes           | 201, 400       |
| `PUT`    | `/tasks/{id}`  | Update a task by ID      | Yes           | 200, 400, 404  |
| `DELETE` | `/tasks/{id}`  | Delete a task by ID      | Yes           | 204, 404       |

#### Query parameters for `GET /tasks`

| Parameter   | Type      | Description                                                    |
|-------------|-----------|----------------------------------------------------------------|
| `completed` | `boolean` | Filter by completion status (`true` / `false`)                 |
| `priority`  | `enum`    | Filter by priority (`LOW`, `MEDIUM`, `HIGH`)                   |
| `dueBefore` | `date`    | Return tasks due on or before this date (`YYYY-MM-DD`)         |
| `search`    | `string`  | Free-text search over title and description (case-insensitive) |
| `page`      | `int`     | Page number (0-based). Default: `0`                            |
| `size`      | `int`     | Page size. Default: `20`                                       |
| `sort`      | `string`  | Sort field and direction, e.g. `dueDate,asc`                   |

### Task fields

| Field         | Type      | Required | Description                                   |
|---------------|-----------|----------|-----------------------------------------------|
| `title`       | `string`  | Yes      | Task title                                    |
| `description` | `string`  | No       | Optional details                              |
| `completed`   | `boolean` | No       | Completion status. Defaults to `false`        |
| `priority`    | `enum`    | No       | `LOW`, `MEDIUM`, `HIGH`. Defaults to `MEDIUM` |
| `dueDate`     | `date`    | No       | Due date in `YYYY-MM-DD` format               |

### Example: full flow

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"secret123"}'
# → { "token": "eyJhbGci...", "username": "admin" }

# 2. Create a task using the token
curl -X POST http://localhost:8080/tasks \
     -H "Authorization: Bearer eyJhbGci..." \
     -H "Content-Type: application/json" \
     -d '{"title":"Buy groceries","priority":"HIGH","dueDate":"2026-12-31"}'

# 3. List tasks with filters
curl "http://localhost:8080/tasks?priority=HIGH&sort=dueDate,asc" \
     -H "Authorization: Bearer eyJhbGci..."
```

---

## Swagger UI

Interactive API documentation available at:

```
http://localhost:8080/swagger-ui.html
```

Click **Authorize** to enter credentials. Two schemes are supported:
- **basicAuth** — enter username and password directly.
- **bearerAuth** — paste a token obtained from `POST /auth/login`.

Swagger UI itself is publicly accessible (no login required).

---

## Project Structure

```
src/
├── main/java/taskmanager/
│   ├── config/              # Security and OpenAPI config (SecurityConfig, OpenApiConfig)
│   ├── controller/          # REST endpoints (TaskController, AuthController)
│   ├── dto/                 # Request/response DTOs
│   │   └── auth/            # LoginRequest, RegisterRequest, JwtAuthResponse
│   ├── exception/           # Global error handling (GlobalExceptionHandler, TaskNotFoundException)
│   ├── model/               # JPA entities (Task, User, Priority)
│   ├── repository/          # Spring Data repositories (TaskRepository, UserRepository)
│   ├── security/            # JWT utilities and filter (JwtUtils, JwtAuthFilter)
│   ├── service/             # Business logic (TaskService, CustomUserDetailsService)
│   └── specification/       # JPA Specifications for dynamic filtering (TaskSpecification)
└── test/
    └── java/taskmanager/
        ├── config/          # Test security config (TestSecurityConfig)
        └── controller/      # Integration tests (MockMvc + H2)
```

---

## License

[MIT](LICENSE)

---

## Requirements

- Java 17+
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

> **Tip:** If port 8080 is already in use, kill the previous process first:
> ```bash
> fuser -k 8080/tcp; ./mvnw spring-boot:run
> ```

---

## Authentication

All endpoints except Swagger UI require **HTTP Basic** authentication.

Users are stored in the `users` table with BCrypt-encoded passwords. To insert a user directly in PostgreSQL:

```sql
INSERT INTO users (username, password)
VALUES ('admin', '$2a$10$<bcrypt_hash>');
```

Generate a BCrypt hash with:

```bash
htpasswd -bnBC 10 "" yourpassword | tr -d ':\n'
```

---

## Run tests

Tests run against an in-memory H2 database — no external services or authentication required.

```bash
./mvnw test
```

SQL statements executed during tests are printed to the console (`spring.jpa.show-sql=true` in the test profile).

---

## API Endpoints

### Tasks

| Method   | Endpoint       | Description              | Status codes   |
|----------|----------------|--------------------------|----------------|
| `GET`    | `/tasks`       | List / filter tasks      | 200            |
| `GET`    | `/tasks/{id}`  | Get a task by ID         | 200, 404       |
| `POST`   | `/tasks`       | Create a new task        | 201, 400       |
| `PUT`    | `/tasks/{id}`  | Update a task by ID      | 200, 400, 404  |
| `DELETE` | `/tasks/{id}`  | Delete a task by ID      | 204, 404       |

#### Query parameters for `GET /tasks`

| Parameter   | Type      | Description                                              |
|-------------|-----------|----------------------------------------------------------|
| `completed` | `boolean` | Filter by completion status (`true` / `false`)           |
| `priority`  | `enum`    | Filter by priority (`LOW`, `MEDIUM`, `HIGH`)             |
| `dueBefore` | `date`    | Return tasks due on or before this date (`YYYY-MM-DD`)   |
| `search`    | `string`  | Free-text search over title and description (case-insensitive) |
| `page`      | `int`     | Page number (0-based). Default: `0`                      |
| `size`      | `int`     | Page size. Default: `20`                                 |
| `sort`      | `string`  | Sort field and direction, e.g. `dueDate,asc`             |

### Task fields

| Field         | Type      | Required | Description                                   |
|---------------|-----------|----------|-----------------------------------------------|
| `title`       | `string`  | Yes      | Task title                                    |
| `description` | `string`  | No       | Optional details                              |
| `completed`   | `boolean` | No       | Completion status. Defaults to `false`        |
| `priority`    | `enum`    | No       | `LOW`, `MEDIUM`, `HIGH`. Defaults to `MEDIUM` |
| `dueDate`     | `date`    | No       | Due date in `YYYY-MM-DD` format               |

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

Click **Authorize** and enter your credentials to test protected endpoints directly from the browser. Swagger UI itself is publicly accessible (no login required).

---

## Project Structure

```
src/
├── main/java/taskmanager/
│   ├── config/              # Security (SecurityConfig, OpenApiConfig)
│   ├── controller/          # REST endpoints (TaskController)
│   ├── dto/                 # Request/response DTOs
│   │   └── auth/            # LoginRequest, RegisterRequest
│   ├── exception/           # Global error handling (GlobalExceptionHandler, TaskNotFoundException)
│   ├── model/               # JPA entities (Task, User, Priority)
│   ├── repository/          # Spring Data repositories (TaskRepository, UserRepository)
│   ├── service/             # Business logic (TaskService, CustomUserDetailsService)
│   └── specification/       # JPA Specifications for dynamic filtering (TaskSpecification)
└── test/
    └── java/taskmanager/
        ├── config/          # Test security config (TestSecurityConfig)
        └── controller/      # Integration tests (MockMvc + H2)
```

---

## License

[MIT](LICENSE)

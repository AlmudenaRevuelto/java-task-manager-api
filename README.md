# Task Manager

Full-stack task management application with a Spring Boot REST API backend and a React + TypeScript frontend. Supports full CRUD operations with filtering, pagination, sorting, free-text search, JWT authentication, role-based access control, an admin panel, and Docker deployment.

## Demo

[Watch the demo on YouTube](https://youtu.be/QhkHOAJCav8)

---

## Tech Stack

### Backend
- **Java 17** — LTS runtime
- **Spring Boot 4.0** — web, JPA, validation, security, devtools
- **Spring Security** — JWT Bearer authentication, role-based access control (`USER` / `ADMIN`), BCrypt password encoding
- **JJWT 0.11.5** — JWT token generation and validation
- **PostgreSQL** — production database
- **H2** — in-memory database for tests
- **springdoc-openapi 3** — Swagger UI with Bearer token support
- **Maven Wrapper** — reproducible builds

### Frontend
- **React 19 + TypeScript** — UI
- **Vite 8** — build tool and dev server
- **React Router 7** — client-side routing with protected routes
- **Axios 1.15** — HTTP client with JWT interceptor
- **react-hot-toast** — user-facing notifications
- **Vitest 4** — unit / component tests

### Infrastructure
- **Docker Compose** — three services: `db` (PostgreSQL), `backend` (Spring Boot), `frontend` (nginx)
- **nginx** — serves the React SPA and reverse-proxies `/auth`, `/tasks`, `/admin` to the backend

---

## Requirements

- Docker & Docker Compose (recommended)
- **Or** Java 17+, Maven 3.9+, Node.js 20+, and a running PostgreSQL instance

---

## Run with Docker (recommended)

```bash
docker compose up --build
```

| Service  | URL                                   |
|----------|---------------------------------------|
| Frontend | http://localhost:80                   |
| Backend  | http://localhost:8080                 |
| Swagger  | http://localhost:8080/swagger-ui.html |

---

## Run locally (without Docker)

### Backend

Start a PostgreSQL instance and configure the following environment variables (or edit `src/main/resources/application.properties`):

| Variable     | Default                                  | Description                       |
|--------------|------------------------------------------|-----------------------------------|
| `DB_HOST`    | `localhost`                              | PostgreSQL host                   |
| `DB_PORT`    | `5432`                                   | PostgreSQL port                   |
| `DB_NAME`    | `taskdb`                                 | Database name                     |
| `DB_USER`    | `postgres`                               | Database user                     |
| `DB_PASS`    | `postgres`                               | Database password                 |
| `JWT_SECRET` | `change-me-in-production-min-32-chars!!` | JWT signing secret (min 32 chars) |

```bash
./mvnw spring-boot:run
```

> **Tip:** If port 8080 is already in use: `fuser -k 8080/tcp`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Dev server runs at `http://localhost:5173`. API calls are proxied to `http://localhost:8080`.

---

## Authentication & Roles

The API uses **JWT Bearer** tokens. Every protected endpoint requires:

```
Authorization: Bearer <token>
```

Tokens expire after **24 hours** (configurable via `app.jwt.expiration-ms`).

### Roles

| Role    | Permissions                                                        |
|---------|--------------------------------------------------------------------|
| `USER`  | Create, read, update, and delete their own tasks                   |
| `ADMIN` | Same as USER (scoped to own tasks) + access to the admin endpoints |

> Task visibility is always scoped to the authenticated user. Admins only see their own tasks in the task list.

### Register

```
POST /auth/register
Body: { "username": "alice", "password": "secret123" }
→ 201 { "token": "eyJhbGci...", "username": "alice", "role": "USER" }
```

New users are always assigned the `USER` role. An admin can promote them via `PUT /admin/users/{id}/role`.

### Login

```
POST /auth/login
Body: { "username": "alice", "password": "secret123" }
→ 200 { "token": "eyJhbGci...", "username": "alice", "role": "USER" }
```

### Create a user directly in PostgreSQL

```sql
INSERT INTO users (username, password, role)
VALUES ('alice', '$2a$10$<bcrypt_hash>', 'USER');
```

Generate a BCrypt hash:

```bash
htpasswd -bnBC 10 "" yourpassword | tr -d ':\n'
```

---

## API Endpoints

### Auth

| Method | Endpoint         | Description                      | Auth | Status codes |
|--------|------------------|----------------------------------|------|--------------|
| `POST` | `/auth/register` | Register a new user, returns JWT | No   | 201, 400     |
| `POST` | `/auth/login`    | Authenticate and return JWT      | No   | 200, 401     |

---

### Tasks

All task endpoints are scoped to the authenticated user — regardless of role, each user only sees and manages their own tasks.

| Method   | Endpoint      | Description         | Auth | Status codes  |
|----------|---------------|---------------------|------|---------------|
| `GET`    | `/tasks`      | List / filter tasks | Yes  | 200           |
| `GET`    | `/tasks/{id}` | Get a task by ID    | Yes  | 200, 404      |
| `POST`   | `/tasks`      | Create a new task   | Yes  | 201, 400      |
| `PUT`    | `/tasks/{id}` | Update a task       | Yes  | 200, 400, 404 |
| `DELETE` | `/tasks/{id}` | Delete a task       | Yes  | 204, 404      |

#### Query parameters for `GET /tasks`

| Parameter   | Type      | Description                                                    |
|-------------|-----------|----------------------------------------------------------------|
| `completed` | `boolean` | Filter by completion status (`true` / `false`)                 |
| `priority`  | `enum`    | Filter by priority (`LOW`, `MEDIUM`, `HIGH`)                   |
| `dueBefore` | `date`    | Return tasks due on or before this date (`YYYY-MM-DD`)         |
| `search`    | `string`  | Free-text search over title and description (case-insensitive) |
| `page`      | `int`     | Page number (0-based). Default: `0`                            |
| `size`      | `int`     | Page size. Default: `12`                                       |
| `sort`      | `string`  | Sort field and direction, e.g. `dueDate,asc`                   |

#### Task fields

| Field         | Type      | Required | Description                                   |
|---------------|-----------|----------|-----------------------------------------------|
| `title`       | `string`  | Yes      | Task title                                    |
| `description` | `string`  | No       | Optional details                              |
| `completed`   | `boolean` | No       | Completion status. Defaults to `false`        |
| `priority`    | `enum`    | No       | `LOW`, `MEDIUM`, `HIGH`. Defaults to `MEDIUM` |
| `dueDate`     | `date`    | No       | Due date in `YYYY-MM-DD` format               |

---

### Admin

Accessible only by users with the `ADMIN` role. Returns 403 for any other role.

| Method   | Endpoint                 | Description                             | Auth (ADMIN) | Status codes       |
|----------|--------------------------|-----------------------------------------|--------------|--------------------|
| `GET`    | `/admin/users`           | List all registered users               | Yes          | 200, 403           |
| `PUT`    | `/admin/users/{id}/role` | Change a user's role (`USER` / `ADMIN`) | Yes          | 200, 400, 403, 404 |
| `DELETE` | `/admin/users/{id}`      | Delete a user and all their tasks       | Yes          | 204, 400, 403, 404 |

> `DELETE /admin/users/{id}` returns **400** if the admin tries to delete their own account.

---

## Example: full flow

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","password":"secret123"}'
# → { "token": "eyJhbGci...", "username": "alice", "role": "USER" }

# 2. Create a task
curl -X POST http://localhost:8080/tasks \
     -H "Authorization: Bearer eyJhbGci..." \
     -H "Content-Type: application/json" \
     -d '{"title":"Buy groceries","priority":"HIGH","dueDate":"2026-12-31"}'

# 3. List tasks with filters
curl "http://localhost:8080/tasks?priority=HIGH&sort=dueDate,asc" \
     -H "Authorization: Bearer eyJhbGci..."

# 4. Promote alice to ADMIN (requires an existing admin token)
curl -X PUT http://localhost:8080/admin/users/2/role \
     -H "Authorization: Bearer <admin_token>" \
     -H "Content-Type: application/json" \
     -d '{"role":"ADMIN"}'

# 5. Delete a user (requires ADMIN token; cannot delete yourself)
curl -X DELETE http://localhost:8080/admin/users/3 \
     -H "Authorization: Bearer <admin_token>"
```

---

## Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Click **Authorize** and paste a token obtained from `POST /auth/login`. Swagger UI is publicly accessible.

---

## Run tests

Tests run against an in-memory H2 database — no external services required.

```bash
# Backend
./mvnw test

# Frontend
cd frontend && npm test
```

---

## Project Structure

```
├── docker-compose.yml
├── Dockerfile                    # Backend multi-stage build
├── pom.xml
├── src/
│   ├── main/java/taskmanager/
│   │   ├── config/               # SecurityConfig, OpenApiConfig, EncoderConfig, MethodSecurityConfig
│   │   ├── controller/           # AuthController, TaskController, AdminController
│   │   ├── dto/                  # TaskRequest, TaskResponse, UserResponse, UpdateRoleRequest
│   │   │   └── auth/             # LoginRequest, RegisterRequest, JwtAuthResponse
│   │   ├── exception/            # GlobalExceptionHandler, TaskNotFoundException, AccessDeniedException
│   │   ├── model/                # Task, User, Priority, Role
│   │   ├── repository/           # TaskRepository, UserRepository
│   │   ├── security/             # JwtUtils, JwtAuthFilter, SecurityHelper
│   │   ├── service/              # TaskService, CustomUserDetailsService
│   │   └── specification/        # TaskSpecification (dynamic JPA filtering)
│   └── test/java/taskmanager/
│       ├── config/               # TestSecurityConfig
│       └── controller/           # AuthControllerIntegrationTest, TaskControllerIntegrationTest
└── frontend/
    ├── Dockerfile                # Frontend multi-stage build (Vite → nginx)
    ├── nginx.conf                # SPA routing + reverse proxy for /auth, /tasks, /admin
    └── src/
        ├── api/                  # authApi.ts, taskApi.ts, adminApi.ts, axiosConfig.ts
        ├── auth/                 # AuthContext.tsx (token, username, role)
        ├── components/
        │   ├── Modal/            # Reusable modal (supports className prop for sizing)
        │   ├── RegisterModal/    # New-user registration form
        │   ├── EditTaskModal/    # Edit existing task
        │   └── UsersModal/       # Admin panel: list users, change roles, delete users
        ├── pages/
        │   ├── LoginPage/        # Login form + register modal link
        │   └── TaskPage/         # Task list, filters, pagination; "Gestionar usuarios" for ADMINs
        └── types/                # task.ts
```

---

## License

[MIT](LICENSE)

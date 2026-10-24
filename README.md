# HireTrack – Job Application Tracker

[![CI](https://github.com/bhavadharanithangaraj/hiretrack/actions/workflows/ci.yml/badge.svg)](https://github.com/bhavadharanithangaraj/hiretrack/actions/workflows/ci.yml)

HireTrack is a full-stack web app for keeping track of your job applications. You can register, log in, record every application (company, role, location, status, date, notes), filter them by status, and see a dashboard of how many are at each stage. Admins can also view all registered users.

It's built with a **Spring Boot REST API** secured with **JWT**, a **PostgreSQL** database, and a **Next.js + Material UI** frontend, all runnable with a single `docker compose up`.

## Tech Stack

| Layer      | Technologies |
|------------|--------------|
| Backend    | Java 17, Spring Boot 3, Spring Web (REST), Spring Data JPA + Hibernate, Bean Validation |
| Security   | Spring Security, JWT (jjwt), BCrypt password hashing, role-based access (`ROLE_USER`, `ROLE_ADMIN`) |
| Database   | PostgreSQL 16 |
| API docs   | Swagger UI / OpenAPI (springdoc) |
| Testing    | JUnit 5, Mockito, MockMvc |
| Frontend   | Next.js 15 (App Router), React 19, TypeScript, Material UI 7 |
| DevOps     | Docker (multi-stage builds), Docker Compose, GitHub Actions CI |

## Features

- **Authentication**: register and log in; the API returns a JWT that the frontend sends as `Authorization: Bearer <token>`. Passwords are stored as BCrypt hashes.
- **Job applications CRUD**: create, view, edit, and delete applications with company, role, location, status (`APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED`), applied date, and notes.
- **Data isolation**: every query is scoped to the logged-in user, so users can only see and change their own applications.
- **Dashboard**: count of applications per status, shown as cards.
- **Status filter** on the applications table.
- **Admin only**: list all users (`@PreAuthorize("hasRole('ADMIN')")`). An admin account is seeded on startup.
- **Validation and error handling**: Bean Validation on all request bodies, plus a global `@ControllerAdvice` that returns one consistent JSON error shape with per-field messages.
- **Frontend**: login, register, dashboard, applications table, and an add/edit form with client-side validation, loading spinners, and error alerts.

## Architecture

```
 ┌──────────────────────┐   HTTP + JSON    ┌─────────────────────────────────────────┐   JDBC   ┌──────────────┐
 │   Next.js frontend   │  Authorization:  │          Spring Boot backend            │          │  PostgreSQL  │
 │  (React, TypeScript, │  Bearer <JWT>    │                                         │          │              │
 │    Material UI)      │ ───────────────▶ │  JwtAuthenticationFilter                │          │  users       │
 │                      │                  │        │                                │          │  job_        │
 │  localhost:3000      │ ◀─────────────── │        ▼                                │ ───────▶ │  applications│
 └──────────────────────┘                  │  Controller ─▶ Service ─▶ Repository ───┼────────▶ │              │
                                           │  (REST, @Valid)  (logic)   (Spring Data)│          │ localhost:   │
                                           │                                         │          │ 5432         │
                                           │  GlobalExceptionHandler (@ControllerAdvice)        └──────────────┘
                                           │  localhost:8080  ·  /swagger-ui.html    │
                                           └─────────────────────────────────────────┘
```

Backend packages (`backend/src/main/java/com/hiretrack`):

| Package      | What's inside |
|--------------|---------------|
| `controller` | REST endpoints: auth, applications, dashboard, admin, health |
| `service`    | Business logic: `AuthService`, `JobApplicationService`, `UserService` |
| `repository` | Spring Data JPA interfaces (SQL generated from method names) |
| `entity`     | JPA entities `User`, `JobApplication` and the `Role` / `ApplicationStatus` enums |
| `dto`        | Request/response records with validation annotations |
| `security`   | `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService` |
| `exception`  | Custom exceptions and the global `@RestControllerAdvice` |
| `config`     | Spring Security, CORS, Swagger, and the admin `DataSeeder` |

## API Endpoints

All endpoints except `/api/auth/**` and `/api/health` require an `Authorization: Bearer <token>` header.
Interactive docs: **http://localhost:8080/swagger-ui.html** (click **Authorize** and paste a token).

| Method | Endpoint                         | Access | Description |
|--------|----------------------------------|--------|-------------|
| POST   | `/api/auth/register`             | Public | Create an account → returns JWT |
| POST   | `/api/auth/login`                | Public | Log in → returns JWT |
| GET    | `/api/health`                    | Public | Health check |
| GET    | `/api/applications`              | User   | List your applications (optional `?status=INTERVIEW`) |
| GET    | `/api/applications/{id}`         | User   | Get one of your applications |
| POST   | `/api/applications`              | User   | Create an application |
| PUT    | `/api/applications/{id}`         | User   | Update an application |
| DELETE | `/api/applications/{id}`         | User   | Delete an application |
| GET    | `/api/dashboard`                 | User   | Count of your applications by status |
| GET    | `/api/admin/users`               | Admin  | List all users |

Example request body for `POST /api/applications`:

```json
{
  "company": "Google",
  "role": "Frontend Engineer",
  "location": "Bangalore",
  "status": "APPLIED",
  "appliedDate": "2026-10-20",
  "notes": "Referred by a friend"
}
```

Errors always look like this:

```json
{
  "timestamp": "2026-10-24T09:28:15Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": { "company": "Company is required" }
}
```

## Running with Docker (recommended)

Requirements: [Docker Desktop](https://www.docker.com/products/docker-desktop/).

```bash
git clone https://github.com/bhavadharanithangaraj/hiretrack.git
cd hiretrack
docker compose up --build
```

Then open:

- Frontend: **http://localhost:3000**
- Swagger UI: **http://localhost:8080/swagger-ui.html**

A default admin account is created on first start:

| Email                 | Password      |
|-----------------------|---------------|
| `admin@hiretrack.com` | `Admin@12345` |

To change the secrets or admin credentials, create a `.env` file next to `docker-compose.yml`:

```env
JWT_SECRET=a-long-random-string-of-at-least-32-characters
ADMIN_PASSWORD=SomethingStrong!
DB_PASSWORD=another-password
```

Stop with `docker compose down` (add `-v` to also delete the database).

## Running locally without Docker for the apps

Requirements: Java 17, Maven 3.9+, Node.js 22, and a PostgreSQL database (easiest: `docker compose up -d postgres`).

```bash
# Terminal 1 – backend on http://localhost:8080
cd backend
mvn spring-boot:run

# Terminal 2 – frontend on http://localhost:3000
cd frontend
npm install
npm run dev
```

## Running Tests

```bash
cd backend
mvn test
```

21 tests, no database needed:

- `JobApplicationServiceTest`: CRUD logic, ownership checks, status filter, dashboard counts (Mockito)
- `AuthServiceTest`: BCrypt hashing, default role, duplicate email, bad credentials (Mockito)
- `JwtServiceTest`: valid, forged, expired, and malformed tokens
- `JobApplicationControllerTest`: 401 without login, 400 validation errors, 201 create, 404 not owned (MockMvc)
- `AdminControllerTest`: admin endpoint returns 403 for users and 200 for admins (MockMvc)

GitHub Actions runs `mvn test` and the frontend build on every push and pull request ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)).

## Screenshots

_Screenshots coming soon._

<!--
To add screenshots: save them in docs/screenshots/ and uncomment this table.

| Login | Dashboard |
|-------|-----------|
| ![Login](docs/screenshots/login.png) | ![Dashboard](docs/screenshots/dashboard.png) |

| Applications | Add / Edit |
|--------------|------------|
| ![Applications](docs/screenshots/applications.png) | ![Form](docs/screenshots/form.png) |
-->

| Page | What it shows |
|------|---------------|
| Login / Register | Auth forms with validation |
| Dashboard | Status count cards (Total, Applied, Interview, Offer, Rejected) |
| Applications | MUI table with status filter, edit and delete actions |
| Add / Edit | Application form with client-side and server-side validation |

## Project Structure

```
hiretrack/
├── backend/                 Spring Boot API (Maven)
│   ├── src/main/java/com/hiretrack/
│   ├── src/test/java/com/hiretrack/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                Next.js app
│   ├── src/app/             Pages (App Router)
│   ├── src/components/      Reusable UI components
│   ├── src/context/         Auth context
│   ├── src/lib/             API client and types
│   └── Dockerfile
├── .github/workflows/ci.yml
├── docker-compose.yml
└── README.md
```

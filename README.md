# reciMe API

A personal recipe management REST API — users can create, search, and manage their own recipes.

---

## Quick Start

**Prerequisites:** Docker Desktop running.

```bash
git clone <repo-url>
cd recime-api
cp .env.example .env        # fill in your DB credentials
docker compose up --build
```

Starts both PostgreSQL and the Spring Boot app. Credentials come from `.env` — see [Credentials](#credentials) below.

| | URL |
| --- | --- |
| App | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui/index.html` |
| Health | `http://localhost:8080/actuator/health` |

For non-Docker runs, see [Local Setup](#local-setup) below.

---

## Tech Stack

| Layer | Choice |
| --- | --- |
| Runtime | Java 17 · Spring Boot 3.5.0 |
| Persistence | JPA/Hibernate · PostgreSQL 14.17 |
| Build | Maven · Lombok |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Ops | Actuator (health/info) · Docker (non-root, multi-stage) |
| Tests | JUnit 5 · Mockito · Spring Test |

---

## API

Base path: `/api/v1/recipes`. All endpoints require an `X-User-Id: <uuid>` header — see [Design Decision 1](#1-recipe-ownership-via-header--auth-out-of-scope).

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/` | Create a recipe |
| GET | `/{id}` | Fetch one |
| GET | `/` | List + search |
| PUT | `/{id}` | Full replacement |
| DELETE | `/{id}` | Delete |

**Search filters** (all optional, AND-combined): `isVegetarian`, `servings`, `include` (repeatable), `exclude` (repeatable), `instruction` (keyword). Plus `page`, `size`, `sort`.

Full schemas and a "Try it out" console are in Swagger.

---

## Architecture

```text
Controller  →  Service  →  Repository  →  PostgreSQL
    ↕               ↕            ↕
  DTOs           Entity     Specification
    ↕______________↕
        Mapper
```

- **Controller** — HTTP only: headers, validation, status codes
- **Service** — `@Transactional` boundaries, business logic, ownership scoping
- **Repository** — Spring Data JPA + `JpaSpecificationExecutor` for dynamic search
- **Specification** — one composable predicate per search filter; absent = no-op
- **Mapper** — Entity ↔ DTO translation; entities never reach the HTTP layer
- **GlobalExceptionHandler** — `@RestControllerAdvice`; every error returns RFC 7807 `ProblemDetail`

---

## Data Model

```text
recipes
├── id            BIGSERIAL PK
├── user_id       UUID        NOT NULL   INDEX idx_recipes_user_id
├── title         VARCHAR     NOT NULL
├── description   VARCHAR
├── servings      INTEGER     default 0
├── is_vegetarian BOOLEAN     default false
├── created_at    TIMESTAMP
└── updated_at    TIMESTAMP

ingredients
├── id         BIGSERIAL PK
├── recipe_id  BIGINT FK → recipes.id   INDEX idx_ingredients_recipe_id
├── name       VARCHAR NOT NULL
├── quantity   VARCHAR (optional)
└── unit       VARCHAR (optional)

instructions
├── id          BIGSERIAL PK
├── recipe_id   BIGINT FK → recipes.id
├── step_order  INTEGER NOT NULL
└── description VARCHAR NOT NULL
     UNIQUE (recipe_id, step_order)
```

```text
Recipe (1) ──< Ingredient   (N)
Recipe (1) ──< Instruction  (N)
```

Deleting a recipe removes its ingredients and instructions too (cascade delete).

**Indexes:**

| Index | Why |
| --- | --- |
| `idx_recipes_user_id` | Every query filters by `userId` — without this it's a full table scan |
| `idx_ingredients_recipe_id` | PostgreSQL doesn't auto-index FK columns; hit on every collection load |
| `UNIQUE(recipe_id, step_order)` | Enforces unique step order per recipe; implicit index also covers `recipe_id` lookups |

---

## Design Decisions

### 1. Recipe ownership via header — auth out of scope

Users can manage their own recipes thus all queries are scoped to a `userId` from the `X-User-Id` header. In production this becomes Spring Security + JWT; the service layer doesn't change.

### 2. Single endpoint for list and search

All search filters are optional — no filters means list all, some filters means search. Splitting into `/` and `/search` would duplicate the pagination and ownership logic for no gain. `JpaSpecificationExecutor` handles this cleanly: each filter builds a predicate only when the param is present.

### 3. `Set` over `List` for child collections

Instructions use `stepOrder` for ordering, and ingredients have no meaningful order at all — collection position doesn't matter for either. `Set` also prevents duplicate entries at the collection level.

### 4. PUT only — no PATCH

Editing a recipe means changing the whole thing — title, ingredients, steps together. PUT is the right fit: the client sends the full updated state and the server replaces it. PATCH is left out for now; the client is expected to send the full payload.

### 5. Hard delete

No audit requirement, no recovery needed. When a recipe is deleted it should just be gone.

### 6. Consistent error format

Without a shared error format, failures come back as plain strings, HTML, or inconsistent JSON depending on where they're thrown. Spring Boot's built-in `ProblemDetail` lets one `@RestControllerAdvice` handle everything and always return the same shape — including a field breakdown for validation errors.

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Recipe not found"
}
```

---

## Assumptions

1. Auth is out of scope; `X-User-Id` is trusted as-is
2. Ingredient parsing (name / quantity / unit) is the client's job
3. Duplicate recipe titles per user are allowed
4. `isVegetarian` is user-set, not derived from ingredients
5. All search filters AND together
6. `include` requires all listed ingredients; `exclude` requires none; empty = filter off
7. `stepOrder` is client-controlled; duplicates within the same recipe return `400`
8. Text search is case-insensitive
9. `quantity` and `unit` on an ingredient are optional ("a pinch of salt")
10. `servings` defaults to `1` if ingredients are provided, `0` otherwise

---

## Testing

```bash
mvn test
```

- **Controller** via `@WebMvcTest` — status codes, headers, validation, filter passthrough, error mapping
- **Service** with mocked repository + mapper — happy paths, not-found, ownership scoping
- **Mapper** — null handling, servings default logic, instruction sort order
- **Specification** — null/empty input returns a no-op spec
- **Exception handler** — each exception maps to the correct `ProblemDetail` shape

40 tests. No integration tests against a real DB. Testcontainers is the right call and is listed under [What I Would Do Next](#what-i-would-do-next).

---

## What I Would Do Next

**Performance:**

- `@EntityGraph` on single-recipe fetch + `@BatchSize` on search to reduce lazy-load queries
- Flat DTO projection for the search path — one SELECT, no child fetches
- Composite `(user_id, created_at DESC)` and functional `LOWER(name)` indexes once there's traffic to measure against

**Production hardening:**

- Flyway migrations replacing `ddl-auto=update`
- Spring Security + JWT replacing the `X-User-Id` header shim
- `PATCH` via JSON Merge Patch for partial updates if needed
- Testcontainers for integration tests against real PostgreSQL

---

## Credentials

Credentials are not committed. Copy the example and fill in your values:

```bash
cp .env.example .env
```

```env
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password
```

`.env` is gitignored. The app reads credentials via `${SPRING_DATASOURCE_*}` environment variables.

---

## Local Setup

**Prerequisites:** Java 17, Maven 3.8+, PostgreSQL 14+ running locally.

1. Create the database:

```sql
CREATE DATABASE recimedb;
CREATE USER recime_user WITH PASSWORD 'yourpassword';
GRANT ALL PRIVILEGES ON DATABASE recimedb TO recime_user;
```

2. Set the environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/recimedb
export SPRING_DATASOURCE_USERNAME=recime_user
export SPRING_DATASOURCE_PASSWORD=yourpassword
```

3. Run with the `dev` profile:

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Or configure the variables in IntelliJ via Run Configuration → Environment Variables.

**Profiles:**

- Default — `INFO` logging, no SQL output
- `dev` — `show-sql`, Hibernate `DEBUG`, bind-param `TRACE`

---

## Notes to Reviewer

Start with `RecipeController` → `RecipeServiceImpl` for the happy path, then `RecipeSpecification` for the search logic — that's where the most interesting work is.

A few things that might look odd but are intentional:

- `X-User-Id` returns `404` on mismatch, not `403` — to avoid leaking whether a recipe exists
- `Set` on child collections — `stepOrder` handles ordering; the mapper sorts on read
- No `@EntityGraph` or `@BatchSize` — kept simple for now, both are listed as next steps
- No H2 in tests — dialect differences make it unreliable against PostgreSQL queries

Happy to discuss any of it.

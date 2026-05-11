# GreenLoop Backend

The backend is a Spring Boot application for GreenLoop APIs, persistence, auth infrastructure, and realtime messaging.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- Google OAuth2 client support
- JWT support with JJWT
- MySQL for default local persistence
- H2 for the `dev` profile
- Spring WebSocket/STOMP
- SpringDoc OpenAPI / Swagger UI
- JUnit/Spring Boot Test

## Project Structure

```text
backend/
├── src/main/java/com/greenloop/
│   ├── auth/             # JWT, OAuth, email verification, auth exceptions
│   ├── config/           # OpenAPI and shared configuration
│   ├── listing/          # Listing controllers, services, repository, DTOs
│   ├── model/            # JPA entities, enums, converters
│   ├── realtime/         # WebSocket/STOMP config and event publishing
│   ├── repository/       # Shared repositories
│   └── reservation/      # Reservation APIs, service logic, schedulers, statuses
├── src/main/resources/   # Spring configuration files
├── src/test/             # Backend tests
├── db_migration.sql      # Schema bootstrap and seed-oriented SQL
└── pom.xml
```

## Run Locally

From the repository root, start MySQL:

```bash
docker compose up -d
```

This starts MySQL on `localhost:3306` with:

```text
database: greenloop
username: root
password: root
```

Then run the backend:

```bash
cd backend
mvn spring-boot:run
```

The backend runs on `http://localhost:8080`.

## Dev Profile

Use the `dev` profile to run with an H2 in-memory database:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The H2 console is available at:

```text
http://localhost:8080/h2-console
```

## Environment Variables

Database:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Auth and OAuth:

```text
JWT_SECRET
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

Mail and verification:

```text
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
FROM_EMAIL
```

Application URLs:

```text
FRONTEND_URL
APP_BASE_URL
```

Defaults are defined in `src/main/resources/application.yml` where appropriate.

## Main Modules

- `listing/`
  Handles listing APIs and listing business logic.

- `reservation/`
  Handles reservation creation, status transitions, no-show tracking, and pickup-related flows.

- `auth/`
  Contains JWT, OAuth, email verification, security configuration, and auth exception handling.

- `realtime/`
  Configures the `/ws` endpoint and publishes listing or reservation events with STOMP/WebSocket support.

- `model/`
  Contains JPA entities, enum-backed domain state, and converters.

## Data Model

The schema in `db_migration.sql` defines the main domain objects:

- `users`
- `listings`
- `reservations`
- `email_verification_tokens`
- support tables such as `refresh_tokens`, `user_roles`, and `audit_logs`

Core relationships:

- one user owns many listings
- one user can create many reservations
- one listing can have reservations over time, subject to status and quantity rules

## Tests

Run backend tests:

```bash
mvn test
```

## API Contract

The shared API contract draft lives at:

```text
../openapi.yaml
```

Keep it aligned with controller behavior when backend endpoints change.

## Realtime Notes

The backend exposes a WebSocket endpoint at:

```text
/ws
```

The realtime foundation exists, but some listing and reservation publish calls are still evolving. Treat this area as in progress and keep frontend `VITE_WS_URL` aligned with backend configuration.

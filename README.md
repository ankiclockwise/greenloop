# Greenloop
# GreenLoop

GreenLoop is a food rescue platform for campuses and nearby communities. It helps donors publish surplus food, lets consumers discover nearby listings, and supports reservation-based pickup before food is wasted.

This repository is a monorepo with:

- a Spring Boot backend for APIs, persistence, auth infrastructure, and realtime messaging
- a React + Vite frontend for discovery, donation, and reservation flows
- shared project artifacts such as the OpenAPI contract, database migration SQL, and CI configuration


# Overview
Important backend environment variables:

Food waste is one of the most overlooked drivers of climate change, responsible for approximately 8 to 10% of global greenhouse gas emissions. When food is discarded, it carries the full carbon cost of everything that went into producing, transporting, and storing it, along with additional methane emissions released as it decomposes in landfills. This problem is especially visible in university settings, where dining halls overproduce daily, grocery stores regularly discard near-expiry items that are still perfectly edible, and students throw away food they simply did not get around to finishing.
GreenLoop is a web-based food rescue platform built to connect surplus food with people who can use it before it goes to waste. It serves four groups: grocery stores with near-expiry inventory, university dining halls with end-of-day surplus, individual donors with extra food, and students or community members looking for affordable meals nearby. The platform aims to reduce food waste on and around college campuses, lower the associated carbon emissions, and improve food access for students on tight budgets. By serving the Five College community across UMass Amherst and neighboring institutions, GreenLoop creates a shared sustainability platform where every rescued meal is tracked as a real reduction in CO2 emissions, making environmental impact visible and meaningful for everyone involved.

# Features
Multi-role user authentication and account management
Food listing creation, discovery, and status lifecycle management
Location-based feed with real-time availability updates
Reservation and pickup verification
Carbon impact tracking and community impact dashboard
Business analytics dashboard for institutional partners
Automated donation mode with push notification alerts

## System Overview

GreenLoop currently centers on four core capabilities:

- listing creation and discovery
- reservation and pickup coordination
- role-aware authentication flows
- realtime feed updates over WebSockets

At a high level, the runtime flow is:

1. Users authenticate in the frontend.
2. The frontend calls backend REST endpoints to fetch or mutate listings and reservations.
3. The backend persists data in MySQL by default, with an H2 in-memory profile available for local development.
4. Listing and reservation changes can be broadcast to connected clients over STOMP/WebSocket endpoints.

## Repository Layout

```text
greenloop/
├── backend/                  # Spring Boot 3 / Java 17 application
│   ├── src/main/java/com/greenloop/
│   │   ├── auth/             # JWT, OAuth, email verification, auth exceptions
│   │   ├── listing/          # Listing controllers, services, repository, update DTO
│   │   ├── reservation/      # Reservation APIs, service logic, schedulers, status flows
│   │   ├── realtime/         # STOMP/WebSocket config and event publishing
│   │   ├── model/            # JPA entities, enums, converters
│   │   └── repository/       # Shared repositories
│   ├── src/main/resources/   # Spring configuration
│   ├── src/test/             # Backend tests
│   ├── db_migration.sql      # Schema bootstrap and seed-oriented SQL
│   └── pom.xml
├── frontend/                 # React 18 / Vite application
│   ├── src/auth/             # Firebase auth wrapper and route guards
│   ├── src/components/feed/  # Feed UI and reservation/listing components
│   ├── src/hooks/            # Reusable browser + realtime hooks
│   ├── src/pages/            # Route-level pages
│   └── src/styles/           # CSS stylesheets used by the app
├── openapi.yaml              # API contract draft
├── docker-compose.yml        # Local MySQL service
├── ci.yml                    # CI pipeline definition
└── DESIGN_NOTES.md           # Team conventions and implementation notes
```

## Architecture

### Backend

The backend follows a mostly layered Spring structure:

- controllers expose HTTP endpoints under `/api/...`
- services hold domain logic for listings and reservations
- repositories use Spring Data JPA for persistence
- models represent users, listings, reservations, and enum-backed state

Key backend modules:

- `listing/`
  Handles CRUD-style listing operations. `ListingService` validates pickup windows, assigns ownership, sets initial status, and saves through `ListingRepository`.
- `reservation/`
  Handles reservation creation and reservation lifecycle support. The repository and scheduler-related classes lay the groundwork for no-show and status tracking flows.
- `auth/`
  Contains JWT filter/provider code, OAuth handlers, email verification, and centralized auth exception handling.
- `realtime/`
  Configures the `/ws` endpoint and publishes feed or reservation events via `SimpMessagingTemplate`.

### Frontend

The frontend is a React SPA built with Vite and `react-router-dom`.

- `App.jsx` defines route-level navigation
- protected/public-only route wrappers gate access by auth state
- `DiscoveryFeed.jsx` is the main product surface for browsing and reserving listings
- custom hooks encapsulate geolocation and WebSocket subscription behavior

The UI is styled with project CSS files in `frontend/src/styles/`; despite older design notes mentioning Tailwind, the current implementation is CSS-based.

### Data and Integration Boundaries

- The backend is the source of truth for listings, reservations, and relational data.
- The frontend currently uses Firebase Auth for client-side sign-in flows.
- The backend also contains JWT and Google OAuth infrastructure, so the authentication story is partially split between frontend and backend implementations at the moment.
- `openapi.yaml` is intended to document the API surface, though it should be kept aligned with the live controllers as the code evolves.

## Runtime Flow

### Listing Discovery

1. The frontend feed requests `/api/listings`.
2. The backend loads available listings from `ListingRepository`.
3. The frontend maps backend listing fields into feed card view models.
4. Filters are applied client-side for category, diet/allergen tags, price, and radius.

### Reservation Flow

1. A user selects a listing in the feed.
2. The frontend posts to `/api/reservations`.
3. `ReservationService` validates that the listing exists and is available.
4. The backend creates a `Reservation` and marks the associated listing as reserved in memory before save.
5. The frontend updates the reservation UI optimistically while the backend APIs continue to mature.

### Realtime Updates

1. The frontend opens a WebSocket connection to `ws://localhost:8080/ws` by default.
2. The hook sends a STOMP `CONNECT` frame.
3. The backend exposes STOMP broker destinations such as `/topic` and `/queue`.
4. `ListingEventPublisher` can broadcast feed updates and reservation notifications.

Note: the realtime foundation exists in the codebase, but some publish calls are still disabled or only partially wired, so this area should be treated as in-progress.

## Data Model

The relational schema in [`backend/db_migration.sql`](/greenloop/backend/db_migration.sql) defines the main domain objects:

- `users`
- `listings`
- `reservations`
- `email_verification_tokens`
- optional support tables such as `refresh_tokens`, `user_roles`, and `audit_logs`

Core relationships:

- one user owns many listings
- one user can create many reservations
- one listing can be reserved by users over time, subject to status rules

## Local Development

### Prerequisites

- Java 17
- Node.js 20 or newer recommended
- npm
- Docker Desktop or a local MySQL 8 instance

### Start the Database

```bash
docker compose up -d
```

This starts MySQL on `localhost:3306` with:

- database: `greenloop`
- username: `root`
- password: `root`

### Backend Setup

From [`backend/`](/greenloop/backend):

```bash
mvn spring-boot:run
```

Useful profiles:

- default profile: MySQL with `spring.jpa.hibernate.ddl-auto=validate`
- `dev` profile: H2 in-memory database with `create-drop`

Run with the dev profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```


### Frontend Setup

From [`frontend/`](/Users/ankitadalvi/codes/greenloop/frontend):

```bash
npm ci
npm run dev
```

The Vite dev server runs on `http://localhost:5173` by default.

Important frontend environment variables:

- `VITE_FIREBASE_API_KEY`
- `VITE_FIREBASE_AUTH_DOMAIN`
- `VITE_FIREBASE_PROJECT_ID`
- `VITE_FIREBASE_STORAGE_BUCKET`
- `VITE_FIREBASE_MESSAGING_SENDER_ID`
- `VITE_FIREBASE_APP_ID`
- `VITE_WS_URL`
- `VITE_USE_MOCK_AUTH`

If `VITE_USE_MOCK_AUTH=true`, the frontend can run without a live Firebase configuration for local UI work.

## Development Process

The repo is structured for iterative feature work inside the monorepo:

1. Define or update the API contract and data model first.
2. Implement backend changes in the relevant controller, service, repository, and model layers.
3. Update the frontend route, hook, or component that consumes the new behavior.
4. Verify the happy path locally before opening a PR.

Recommended working conventions:

- keep business logic in services, not controllers
- keep persistence concerns in repositories
- prefer small, focused DTOs for update/create requests
- keep frontend side effects in hooks or route-level containers
- document externally visible API changes in `openapi.yaml`
- update `README.md` or `DESIGN_NOTES.md` when architectural assumptions change

## Quality and Validation

Backend validation available today:

```bash
cd backend
mvn test
```

Frontend validation available today:

```bash
cd frontend
npm run build
```

The repository also includes [`ci.yml`](/Users/ankitadalvi/codes/greenloop/ci.yml), which outlines a broader CI/CD process for backend tests, frontend build verification, security scanning, and container builds. Some CI steps reference scripts or Docker assets that should be kept in sync with the actual repository contents as the project matures.

## Current Architectural Notes

A few parts of the system are still evolving, and new contributors should be aware of them:

- authentication is split between frontend Firebase flows and backend JWT/OAuth infrastructure
- the realtime subsystem is present, but not every listing/reservation mutation publishes events yet
- some design notes describe future-state conventions that do not fully match the current frontend implementation
- the OpenAPI document and CI pipeline should be treated as important project artifacts, but they need regular maintenance to stay aligned with the running code

## Where To Start

If you are new to the project, begin here:

- read [`DESIGN_NOTES.md`] for team conventions
- inspect [`backend/src/main/java/com/greenloop/listing`] and [`backend/src/main/java/com/greenloop/reservation`]) for the core domain logic
- inspect [`frontend/src/pages/DiscoveryFeed.jsx`] for the main user-facing flow
- use [`openapi.yaml`] as the starting point for API discussion and refinement


# environment variables

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `FROM_EMAIL`
- `FRONTEND_URL`
- `APP_BASE_URL`
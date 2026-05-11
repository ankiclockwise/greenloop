# GreenLoop

GreenLoop is a food rescue platform for campuses and nearby communities. It helps donors publish surplus food, lets consumers discover nearby listings, and supports reservation-based pickup before food is wasted.

- a Spring Boot backend for APIs, persistence, auth infrastructure, and realtime messaging
- a React + Vite frontend for discovery, donation, and reservation flows
- shared project artifacts such as the OpenAPI contract, database migration SQL, and CI configuration


# Overview

Food waste is one of the most overlooked drivers of climate change, responsible for approximately 8 to 10% of global greenhouse gas emissions. When food is discarded, it carries the full carbon cost of everything that went into producing, transporting, and storing it, along with additional methane emissions released as it decomposes in landfills. This problem is especially visible in university settings, where dining halls overproduce daily, grocery stores regularly discard near-expiry items that are still perfectly edible, and students throw away food they simply did not get around to finishing.
GreenLoop is a web-based food rescue platform built to connect surplus food with people who can use it before it goes to waste. It serves four groups: grocery stores with near-expiry inventory, university dining halls with end-of-day surplus, individual donors with extra food, and students or community members looking for affordable meals nearby. The platform aims to reduce food waste on and around college campuses, lower the associated carbon emissions, and improve food access for students on tight budgets. By serving the Five College community across UMass Amherst and neighboring institutions, GreenLoop creates a shared sustainability platform where every rescued meal is tracked as a real reduction in CO2 emissions, making environmental impact visible and meaningful for everyone involved.

## Repository

This is a monorepo with:

- [backend](./backend): Spring Boot APIs, persistence, auth infrastructure, and realtime messaging
- [frontend](./frontend): React + Vite app for discovery, donation, reservation, dashboards, and impact views
- [openapi.yaml](./openapi.yaml): API contract draft
- [docker-compose.yml](./docker-compose.yml): local MySQL service
- [DESIGN_NOTES.md](./DESIGN_NOTES.md): team conventions and implementation notes

## Core Features

- Role-aware authentication and onboarding
- Food listing creation and discovery
- Reservation and pickup coordination
- Realtime feed update foundation over WebSockets
- Impact metrics, badges, and leaderboards
- Donor dashboard for created listings and reservation status

## Quick Start

Start the local database:

```bash
docker compose up -d
```

Run the backend:

```bash
cd backend
mvn spring-boot:run
```

Run the frontend:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs at `http://localhost:5173` and the backend runs at `http://localhost:8080`.

## More Docs

- Frontend setup, routes, testing, and environment variables: [frontend/README.md](./frontend/README.md)
- Backend setup, profiles, environment variables, modules, and tests: [backend/README.md](./backend/README.md)

## Notes

- The frontend currently uses Firebase Auth for client-side sign-in flows.
- The backend also contains JWT and Google OAuth infrastructure, so auth integration should be kept aligned as the project evolves.
- Keep [openapi.yaml](./openapi.yaml) updated when backend API behavior changes.

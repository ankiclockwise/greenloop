# GreenLoop Frontend

This frontend uses React, Vite, React Router, and Firebase Authentication for
email/password and Google sign-in flows.

## Tech Stack

- React 18
- Vite
- React Router
- Firebase Authentication
- Axios
- Vitest
- React Testing Library
- jsdom

## Project Structure

```text
frontend/
├── src/
│   ├── auth/             # Firebase auth wrapper and route guards
│   ├── components/feed/  # Feed, listing, and reservation UI
│   ├── components/impact/# Impact metrics, badges, and leaderboard UI
│   ├── data/             # Mock data used while backend APIs mature
│   ├── hooks/            # Geolocation, impact data, and WebSocket hooks
│   ├── pages/            # Route-level pages
│   ├── styles/           # CSS stylesheets
│   └── utils/            # Frontend data normalization helpers
├── tests/                # Unit tests separated from app source
├── vite.config.js
└── package.json
```

## Run locally

1. Create `frontend/.env`
2. Fill in your Firebase web app credentials [sensitive]
3. Install dependencies

```bash
npm install
```

4. Start the dev server

```bash
npm run dev
```

The Vite dev server runs on `http://localhost:5173` by default.

API calls under `/api` are proxied to `http://localhost:8080`.

## Environment Variables

Create `frontend/.env` and set the Firebase values for your project:

```text
VITE_FIREBASE_API_KEY=
VITE_FIREBASE_AUTH_DOMAIN=
VITE_FIREBASE_PROJECT_ID=
VITE_FIREBASE_STORAGE_BUCKET=
VITE_FIREBASE_MESSAGING_SENDER_ID=
VITE_FIREBASE_APP_ID=
```

Optional local development variables:

```text
VITE_WS_URL=ws://localhost:8080/ws
VITE_USE_MOCK_AUTH=true
VITE_USE_MOCK_DATA=true
VITE_DEMO_OWNER_ID=1
```

`VITE_USE_MOCK_AUTH=true` lets the UI run without a live Firebase configuration for local interface work.

## Test locally

Unit tests use Vitest, React Testing Library, jsdom, and mocked API/Firebase
dependencies so they can run without the backend or live Firebase sign-in.

Run the full frontend unit test suite:

```bash
npm test
```

Run tests in watch mode while developing:

```bash
npm run test:watch
```

Run tests with a coverage report:

```bash
npm run test:coverage
```

Test files live in `frontend/tests`, separated from application code in
`frontend/src`. The folder is organized by area:

- `tests/auth`
- `tests/components`
- `tests/data`
- `tests/hooks`
- `tests/pages`
- `tests/utils`

## Current routes

- `/login` for sign in
- `/signup` for account creation
- `/onboarding` for selecting Student, Store, or Diner
- `/` for the protected discovery feed
- `/dashboard` for created listings and reservation status
- `/impact` for impact metrics, badges, and leaderboards

## Build

Create a production build:

```bash
npm run build
```

Preview the production build locally:

```bash
npm run preview
```

## Backend Integration Notes

- Listing and reservation APIs are expected under `/api`.
- Realtime feed updates use `VITE_WS_URL`, defaulting to `ws://localhost:8080/ws`.
- Mock data remains in the frontend while backend endpoints are being finalized.
- When backend auth verification is ready, send the Firebase ID token in the `Authorization` header from API clients.

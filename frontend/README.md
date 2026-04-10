# Frontend Setup

This frontend uses React, Vite, React Router, and Firebase Authentication for
email/password login and signup.

## Run locally

1. Create `frontend/.env`
2. Fill in your Firebase web app credentials [sensitive]
3. Install dependencies with `npm install`
4. Start the dev server with `npm run dev`

## Current routes

- `/login` for sign in
- `/signup` for account creation
- `/` protected landing page after auth

## Next backend integration step

When your Java backend is ready, send the Firebase ID token in the `Authorization`
header from the frontend and verify it on the backend before reading or writing
Postgres user records.

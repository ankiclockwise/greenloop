# GreenLoop – Design Notes for Team 12
> CS 520, Spring 2026 | Read this before writing any code.

## Architecture

Monorepo layout:
```
greenloop/
├── backend/          ← Spring Boot (Java 17)
├── frontend/         ← React 18 + Vite + Tailwind CSS
├── openapi.yaml      ← Single source of truth for all API contracts
└── .github/workflows/ci.yml  ← CI/CD (GitHub Actions)
```

---

## Backend Design Patterns

### 1. Strict Layered Architecture
Every domain follows **Controller → Service → Repository**. No exceptions.
- Controller handles HTTP only (parse request, call service, return response).
- Service owns all business logic and calls the repository.
- Repository is a Spring Data JPA interface — no SQL in services.

### 2. Global Exception Handling
**Never return error responses from controllers directly.**
Throw a typed exception; `AuthExceptionHandler.java` (@RestControllerAdvice) catches it and returns a standardized `ErrorResponse` DTO.

| Exception class | HTTP status |
|---|---|
| `UserAlreadyExistsException` | 409 |
| `AccountNotVerifiedException` | 403 |
| `RateLimitExceededException` | 429 |
| `TokenExpiredException` | 400 |
| `BadCredentialsException` | 401 |

### 3. State Machine for Reservations
All reservation status changes go through `ReservationStatusMachine.java`.
**Do not set reservation status directly anywhere else in the codebase.**

Valid transitions:
```
RESERVED → COLLECTED  (actor: ADMIN — QR scan confirmed)
RESERVED → CANCELLED  (actor: USER — consumer cancels)
RESERVED → NO_SHOW    (actor: SYSTEM — scheduler)
RESERVED → EXPIRED    (actor: SYSTEM — scheduler)
```

### 4. Event Publisher for Real-time Updates
After any DB save that creates or changes a listing/reservation, call `ListingEventPublisher`:
```java
// In ListingService, after listingRepository.save(listing):
listingEventPublisher.publishNewListing(listing);

// In ReservationService, after save:
listingEventPublisher.publishReservationConfirmation(reservation, user);
```
This pushes the update over WebSocket to all connected clients automatically.

### 5. Constructor Injection (not @Autowired on fields)
```java
// ✅ Correct
public class MyService {
    private final MyRepository repo;
    public MyService(MyRepository repo) { this.repo = repo; }
}

// ❌ Wrong
@Autowired
private MyRepository repo;
```

### 6. JWT Auth Flow
Every request hits `JwtAuthenticationFilter` first.
- It reads the `Authorization: Bearer <token>` header.
- Validates the token and sets `SecurityContext`.
- Controllers can use `@AuthenticationPrincipal` to get the current user.

---

## Frontend Design Patterns

### State Management: Local State + Custom Hooks
**No Redux. No Zustand** (yet — add only if JWT/user role needs sharing across many pages).

Pattern used:
- `useState` for component-local state (listings, filters, loading, errors)
- `useCallback` to memoize fetch functions
- Custom hooks for reusable side-effects:
  - `useGeolocation.js` — browser geolocation with manual ZIP fallback
  - `useFeedWebSocket.js` — STOMP WebSocket with auto-reconnect

### API Calls
Always use **Axios** with the `/api/...` base path.
```js
// ✅ Correct
axios.get('/api/listings', { params })
axios.post('/api/reservations', { listingId })

// ❌ Wrong
fetch('http://localhost:8080/listings')
```

### Styling
**Tailwind utility classes only.** No custom CSS files, no inline `style={{}}`.

### Component Structure
```
src/
├── pages/          ← Full page components (one per route)
├── components/     ← Reusable UI components grouped by domain
│   └── feed/
└── hooks/          ← Custom React hooks (side-effects, external state)
```

---

## Environment Variables

Copy `.env.example` (coming soon) and fill in:
```
JWT_SECRET=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
DB_URL=
FRONTEND_URL=http://localhost:5173
```

Never commit `.env` or secrets to the repo.

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Production-ready code only |
| `develop` | Integration branch — merge features here first |
| `feature/<name>-<scope>` | Individual feature branches per person |

PR checklist before requesting review:
- [ ] CI pipeline passes (all tests green)
- [ ] No new `@SuppressWarnings` without a comment
- [ ] No hardcoded secrets or localhost URLs
- [ ] Follows the patterns in this document

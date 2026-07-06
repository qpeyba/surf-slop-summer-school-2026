# AGENTS.md

## Repo at a glance

Polyglot project — three build systems:
- **backend/** — Go REST API (the real code)
- **app/** — Android/Kotlin (project template only, nothing implemented yet; real architecture plan lives in `02-development/FE_ARCHITECTURE.md`)
- **01-analysis/api/** — OpenAPI 3.x specs per domain, checked by `@redocly/cli` (npm)

No CI/CD. No pre-commit hooks. No `opencode.json`.

Go module path: `summer-school-2026/backend` (see `backend/go.mod`).

## Quickstart (backend)

```bash
cp backend/.env.example backend/.env      # .env is gitignored, required for local dev
docker compose --profile db up -d db      # from backend/
make migrate                              # goose migrations
make run                                  # or: go run ./cmd/api
```

Without `.env`, the config.go default `DATABASE_URL` points to `postgres://volna:volna@localhost:5432/volna` — **not** the `chef:chef@...:5432/chef_table` that compose provisions. Either copy `.env.example` or set `DATABASE_URL` explicitly.

## Commands (all run from `backend/`)

| Task | Command |
|------|---------|
| run | `make run` or `go run ./cmd/api` |
| test | `make test` (all packages) |
| lint | `make lint` (`go vet ./...`) |
| fmt | `make fmt` (`gofmt -w ./cmd ./internal`) |
| migrate | `make migrate` (needs `DATABASE_URL`) |
| integration tests | `TEST_DATABASE_URL=postgres://... go test ./internal/http/handlers ./internal/storage/postgres -count=1` |
| race detector | `go test -race ./...` |

Integration tests **skip automatically** when `TEST_DATABASE_URL` is not set — they won't fail, they'll just pass trivially. Integration tests create isolated schemas (`testutil/database.go`).

### Docker Compose (`backend/`)

Profiles: `db`, `app`, `migrations`, `k6`

```bash
docker compose --profile db up -d db
docker compose --profile app up --build       # api + db
docker compose --profile migrations run --rm migrate  # goose migrations
```

### OpenAPI linting (root `package.json`)

```bash
npm --prefix 01-analysis/api install
npx --prefix 01-analysis/api redocly lint
```

Specs are domain-split with cross-file `$ref` links. Broken refs = hard error.

### Android (`./gradlew`)

```bash
./gradlew assembleDebug
./gradlew test
```

The Android project is a bare template — no Compose, no Hilt, no Retrofit. Do not assume any frontend patterns exist yet.

## Architecture notes

### Backend layering — NOT strictly tiered

The layering is `handler → service → storage/postgres` for **auth, profile, booking**. However, **slots and instructors handlers call repositories directly** (no service layer) — they are read-only catalog endpoints.

- **HTTP layer** (`internal/http/`): chi v5 router, middleware (request ID, logging, panic recovery, JSON content-type), machine-readable error codes. Handlers implement a `RouteRegistrar` interface (`Register(r chi.Router)`) — this is how new routes are wired in.
- **Service layer** (`internal/service/`): exists for auth (OTP), booking (create/cancel/transfer/review), and profile. Business logic includes 12h cancellation rule, atomic booking with row locks.
- **Storage** (`internal/storage/postgres/`): raw SQL/pgx, `FOR UPDATE` row locking for concurrency safety. One repository per domain (auth, bookings, slots, instructors, profile). Domain types used by services are defined in the service package (e.g. `auth.Client`, `booking.Booking`), not in a separate models package.
- **Config** (`internal/config/`): env-vars only, no config files.

### Auth behavior

There is a `RequireAuth` middleware in `internal/http/auth.go`, but it is **not wired in the router**. Instead, each handler calls `bearerOrUnauthorized(w, r)` to extract the Bearer token, then passes it to the service layer for session validation. This means auth errors are domain-specific, not a blanket middleware rejection.

### API surface

All endpoints under `/api/v1/` across 5 domains: auth, profile, slots, instructors, bookings. Plus `/healthz` and `/readyz` for infrastructure. `NotFound` and `MethodNotAllowed` responses are in Russian.

### OpenAPI contract

Domain specs live in `01-analysis/api/{domain}/` — each has `api.yaml` (paths) + `models.yaml` (components), linked via relative `$ref`. The `redocly.yaml` at `01-analysis/api/` registers all domains.

## Commit style

Informal, lowercase, pragmatic. No strict conventional commits. Keep it short, write what you actually did.

## Docs are in Russian

All analysis/design docs under `01-analysis/` and `02-development/` are in Russian. Error messages returned by the API are also in Russian.

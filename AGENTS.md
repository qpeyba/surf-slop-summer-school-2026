# AGENTS.md

## Repo at a glance

Polyglot project — three build systems:
- **backend/** — Go REST API (the real code)
- **app/** — Android/Kotlin (project template only, nothing implemented yet; real architecture plan lives in `02-development/FE_ARCHITECTURE.md`)
- **01-analysis/api/** — OpenAPI 3.x specs per domain, checked by `@redocly/cli` (npm)

No CI/CD. No pre-commit hooks. No `opencode.json`.

## Commands (all run from their respective dirs)

### Backend (`backend/`)

| Task | Command |
|------|---------|
| run | `make run` or `go run ./cmd/api` |
| test | `make test` (unit/package) |
| lint | `make lint` (go vet) |
| fmt | `make fmt` |
| migrate | `make migrate` (needs `DATABASE_URL`) |
| integration tests | `TEST_DATABASE_URL=postgres://... go test ./internal/http/handlers ./internal/storage/postgres -count=1` |
| race detector | `go test -race ./...` |

Integration tests **skip automatically** when `TEST_DATABASE_URL` is not set — they won't fail, they'll just pass trivially.

### Docker Compose (`backend/`)

Profiles: `db`, `app`, `migrations`, `k6`

```bash
docker compose --profile db up -d db          # postgres only
docker compose --profile app up --build       # api + db
docker compose --profile migrations run --rm migrate  # goose migrations
```

Default DB: `postgres://chef:chef@localhost:5432/chef_table`

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

### Backend layering

`handler → service → storage/postgres`

- **HTTP layer** (`internal/http/`): chi v5 router, middleware (request ID, logging, panic recovery, Bearer auth, JSON), machine-readable error codes
- **Service layer** (`internal/service/`): business logic (OTP auth, 12h cancellation rule, atomic booking with row locks)
- **Storage** (`internal/storage/postgres/`): raw SQL/pgx, `FOR UPDATE` row locking for concurrency safety, isolated schema per integration test
- **Config** (`internal/config/`): env-vars only, no config files

### API surface

All endpoints under `/api/v1/` across 5 domains: auth, profile, slots, instructors, bookings. Plus `/healthz` and `/readyz` for infrastructure.

### OpenAPI contract

Domain specs live in `01-analysis/api/{domain}/` — each has `api.yaml` (paths) + `models.yaml` (components), linked via relative `$ref`. The `redocly.yaml` at `01-analysis/api/` registers all domains.

## Commit style

Informal, lowercase, pragmatic. Past commits look like:
- `backend should be ready now`
- `more minor be fixes`
- `rewrite backend according my domain`

No strict conventional commits. Keep it short, write what you actually did.

## Docs are in Russian

All analysis/design docs under `01-analysis/` and `02-development/` are in Russian. Don't be surprised.

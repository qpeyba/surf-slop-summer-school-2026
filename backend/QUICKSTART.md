# Backend Quickstart

## Prerequisites

- Go 1.25+
- Docker (with Compose v2)
- PostgreSQL 16 (Docker image used by default)

## Setup

```bash
# 1. Environment config
cp .env.example .env

# 2. Start PostgreSQL
docker compose --profile db up -d db

# 3. Run migrations
make migrate

# 4. Start the API server
make run
```

Server listens on `http://localhost:8080`.

## Make targets

| Command | Description |
|---------|-------------|
| `make run` | Start the API server |
| `make test` | Run all tests (unit + integration) |
| `make lint` | Run `go vet` |
| `make fmt` | Format Go code |
| `make migrate` | Apply goose migrations |

## Docker Compose profiles

All commands run from `backend/`.

```bash
docker compose --profile db up -d db          # PostgreSQL only
docker compose --profile app up --build       # API + DB
docker compose --profile migrations run --rm migrate  # Run migrations
```

## Environment variables

| Variable | Default | Required |
|----------|---------|----------|
| `HTTP_ADDR` | `:8080` | no |
| `DATABASE_URL` | `postgres://chef:chef@localhost:5432/chef_table?sslmode=disable` | yes |
| `TEST_DATABASE_URL` | — | for integration tests |
| `SHUTDOWN_TIMEOUT` | `10` (seconds) | no |

## Integration tests

```bash
TEST_DATABASE_URL=postgres://chef:chef@localhost:5432/chef_table?sslmode=disable go test ./internal/http/handlers ./internal/storage/postgres -count=1
```

Tests create isolated schemas automatically. Skip when `TEST_DATABASE_URL` is unset.

## Health checks

- `GET /healthz`
- `GET /readyz`

## Shutdown

Stop the API server:

```bash
kill $(pgrep -f 'go run ./cmd/api')   # if running via make run
```

Stop and remove PostgreSQL:

```bash
docker stop postgres-slop && docker rm postgres-slop
```

Or with Compose (if `docker compose` is available):

```bash
docker compose down                     # stops API + DB
docker compose --profile db down        # stops DB only
docker compose down -v                  # stops and deletes volumes
```
.PHONY: fmt lint test run migrate

fmt:
	gofmt -w ./cmd ./internal

lint:
	go vet ./...

test:
	go test ./...

run:
	go run ./cmd/api

migrate:
	go run github.com/pressly/goose/v3/cmd/goose@v3.24.1 -dir migrations postgres "$${DATABASE_URL:-postgres://chef:chef@localhost:5432/chef_table?sslmode=disable}" up

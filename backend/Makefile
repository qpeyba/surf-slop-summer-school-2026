.PHONY: fmt lint test run stop restart migrate db-reset

fmt:
	gofmt -w ./cmd ./internal

lint:
	go vet ./...

test:
	go test ./...

run:
	@echo "Запуск API на http://localhost:8080 ..."
	@set -a && . ./.env && set +a && go run ./cmd/api

stop:
	@PID=$$(lsof -ti:8080 2>/dev/null); \
	if [ -n "$$PID" ]; then \
		kill $$PID && echo "API остановлен (PID $$PID)"; \
	else \
		echo "API не запущен"; \
	fi

restart: stop run

migrate:
	go run github.com/pressly/goose/v3/cmd/goose@v3.24.1 -dir migrations postgres "$${DATABASE_URL:-postgres://chef:chef@localhost:5432/chef_table?sslmode=disable}" up

db-reset:
	@echo "Сброс базы данных..."
	@docker exec postgres-slop psql -U chef -d chef_table -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;" 2>/dev/null || echo "Контейнер postgres-slop не найден, пропускаю psql"
	@set -a && . ./.env && set +a && make migrate
	@echo "База данных сброшена и заполнена mock-данными."

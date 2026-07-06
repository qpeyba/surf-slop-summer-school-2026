.PHONY: fmt lint test run stop restart migrate db-reset db-logs db-psql

fmt:
	gofmt -w ./cmd ./internal

lint:
	go vet ./...

test:
	go test ./...

DB_IMAGE ?= postgres:16-alpine
DB_NAME ?= postgres-slop
DB_PORT ?= 5432

run:
	@echo "Запуск базы данных..."
	@docker inspect $(DB_NAME) >/dev/null 2>&1; \
	if [ $$? -ne 0 ]; then \
		docker run -d --name $(DB_NAME) \
			-e POSTGRES_DB=chef_table \
			-e POSTGRES_USER=chef \
			-e POSTGRES_PASSWORD=chef \
			-p $(DB_PORT):5432 \
			$(DB_IMAGE); \
		echo "Контейнер $(DB_NAME) создан"; \
	else \
		running=$$(docker inspect -f '{{.State.Running}}' $(DB_NAME) 2>/dev/null); \
		if [ "$$running" != "true" ]; then \
			docker start $(DB_NAME) && echo "Контейнер $(DB_NAME) запущен"; \
		else \
			echo "Контейнер $(DB_NAME) уже запущен"; \
		fi; \
	fi
	@echo "Ожидание PostgreSQL..."
	@sleep 1
	@for i in $$(seq 1 20); do \
		docker exec $(DB_NAME) psql -U chef -d chef_table -c "SELECT 1" >/dev/null 2>&1 && break; \
		sleep 1; \
	done
	@echo "Запуск API на http://localhost:8080 ..."
	@set -a && . ./.env && set +a && go run ./cmd/api

stop:
	@PID=$$(lsof -ti:8080 2>/dev/null); \
	if [ -n "$$PID" ]; then \
		kill $$PID && echo "API остановлен (PID $$PID)"; \
	else \
		echo "API не запущен"; \
	fi
	@docker inspect $(DB_NAME) >/dev/null 2>&1; \
	if [ $$? -eq 0 ]; then \
		docker stop $(DB_NAME) >/dev/null && echo "Контейнер $(DB_NAME) остановлен"; \
	else \
		echo "Контейнер $(DB_NAME) не существует"; \
	fi

restart: stop run

migrate:
	go run github.com/pressly/goose/v3/cmd/goose@v3.24.1 -dir migrations postgres "$${DATABASE_URL:-postgres://chef:chef@localhost:5432/chef_table?sslmode=disable}" up

db-reset:
	@echo "Сброс базы данных..."
	@docker exec $(DB_NAME) psql -U chef -d chef_table -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;" 2>/dev/null || echo "Контейнер $(DB_NAME) не найден, пропускаю psql"
	@set -a && . ./.env && set +a && make migrate
	@echo "База данных сброшена и заполнена mock-данными."

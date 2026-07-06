# Промпт: Генерация автотестов (Auto Tests)

## Роль
Ты — Senior SDET (Software Development Engineer in Test), который пишет автоматизированные тесты для Go-бэкенда приложения «Шеф-стол». Твоя задача — написать unit-тесты и интеграционные тесты.

## Контекст
Проект на Go 1.25, REST API (chi v5), PostgreSQL 16 (pgx v5).
- Модуль: `summer-school-2026/backend`
- Слои: `handler → service → storage/postgres`
- Интеграционные тесты используют `testutil/database.go` (изолированные схемы)
- Переменная окружения для интеграционных тестов: `TEST_DATABASE_URL`

## Входные артефакты
1. `backend/` — весь исходный код
2. `backend/internal/storage/postgres/testutil/database.go` — хелпер для интеграционных тестов
3. `03-testing/03-test-cases/scenario-tests.json` — сценарные тесты (бизнес-логика для покрытия)
4. `01-analysis/api/` — OpenAPI-контракты (ожидаемые запросы/ответы/ошибки)
5. `02-development/BE_IMPLEMENTATION_PLAN.md` — план BE (BE-11 — тесты)

## Задача

### А. Unit-тесты (в каждом пакете)
Напиши тесты для:
1. **config** — уже есть `config_test.go`, дополни edge-кейсами
2. **http** — middleware (request ID, json content type, CORS), BearerToken extraction
3. **handlers** — все 5 handler-ов с мок-сервисами
4. **service/auth** — OTP request/verify (мок репозитория)
5. **service/booking** — Create/List/Get/Cancel/Transfer/UpsertReview (мок репозитория)
6. **service/profile** — Current/Update (мок репозитория)

### Б. Интеграционные тесты
Напиши тесты с реальной БД (через `TEST_DATABASE_URL`):
1. **storage/postgres** — все репозитории: auth, bookings, slots, instructors, profile
2. **handlers** — HTTP-тесты с реальным роутером и БД
3. **Race conditions** — конкурентное бронирование (должно падать на double booking)
4. **Concurrency** — параллельная отмена + создание

## Правила написания кода
- **Используй стандартный `testing` пакет Go.** Не добавляй сторонние фреймворки.
- **Для моков:** ручные моки (интерфейсы уже есть — `Repository` в каждом сервисе).
- **Интеграционные тесты:** используют `testutil.SetupDB(t)`.
- **Проверяй, что тесты реально тестируют** — не пиши пустых заглушек.
- **В тестах должен быть хотя бы один падающий ассерт на баг.** (См. bug reports)
- **Стиль:** следуй стандартному Go стилю (gofmt, go vet).
- **Названия тестов:** `Test<ИмяФункции>_<Сценарий>`.

## Правила для интеграционных тестов
```go
func TestXxx(t *testing.T) {
    if testing.Short() {
        t.Skip("skipping integration test")
    }
    db := testutil.SetupDB(t)
    // используй db для тестов
}
```

## Формат вывода
Создай тестовые файлы в соответствующих пакетах:

```
backend/
├── internal/
│   ├── config/
│   │   └── config_test.go       ← дополнить
│   ├── http/
│   │   ├── auth_test.go          ← новый
│   │   ├── middleware_test.go    ← новый
│   │   ├── router_test.go        ← новый
│   │   └── error_test.go         ← новый
│   ├── http/handlers/
│   │   ├── auth_test.go          ← новый
│   │   ├── bookings_test.go      ← новый
│   │   ├── slots_test.go         ← новый
│   │   ├── profile_test.go       ← новый
│   │   └── instructors_test.go   ← новый
│   ├── service/auth/
│   │   └── service_test.go       ← новый
│   ├── service/booking/
│   │   └── service_test.go       ← новый
│   ├── service/profile/
│   │   └── service_test.go       ← новый
│   └── storage/postgres/
│       ├── auth_test.go          ← новый
│       ├── bookings_test.go      ← новый (включая concurrency)
│       ├── slots_test.go         ← новый
│       ├── instructors_test.go   ← новый
│       └── profile_test.go       ← новый
```

Каждый тестовый файл должен компилироваться и запускаться с `go test`.

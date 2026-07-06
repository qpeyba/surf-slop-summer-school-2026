# План реализации BE для «Шеф-стол»

## TOC / Todo реализации

- [x] [BE-00. Создать каркас backend-приложения](#be-00-создать-каркас-backend-приложения)
- [x] [BE-01. OpenAPI-контракт как источник истины](#be-01-openapi-контракт-как-источник-истины)
- [x] [BE-02. Реализовать общую HTTP-инфраструктуру](#be-02-реализовать-общую-http-инфраструктуру)
- [x] [BE-03. Спроектировать БД и миграции](#be-03-спроектировать-бд-и-миграции)
- [x] [BE-04. Реализовать Auth: OTP и сессии](#be-04-реализовать-auth-otp-и-сессии)
- [x] [BE-05. Реализовать Profile](#be-05-реализовать-profile)
- [x] [BE-06. Реализовать read-only каталог слотов и инструкторов](#be-06-реализовать-read-only-каталог-слотов-и-инструкторов)
- [x] [BE-07. Реализовать атомарное создание брони](#be-07-реализовать-атомарное-создание-брони)
- [x] [BE-08. Реализовать список и детали броней](#be-08-реализовать-список-и-детали-броней)
- [x] [BE-09. Реализовать отмену, перенос и оценку](#be-09-реализовать-отмену-перенос-и-оценку)
- [x] [BE-10. Довести контрактные ошибки и валидацию](#be-10-довести-контрактные-ошибки-и-валидацию)
- [ ] [BE-11. Добавить полный набор Go-тестов](#be-11-добавить-полный-набор-go-тестов)
- [ ] [BE-12. Добавить k6 performance tests до 300 concurrent users](#be-12-добавить-k6-performance-tests-до-300-concurrent-users)
- [ ] [BE-13. Подготовить локальный запуск и документацию разработчика](#be-13-подготовить-локальный-запуск-и-документацию-разработчика)
- [x] [BE-14. Финальная проверка готовности BE](#be-14-финальная-проверка-готовности-be)

## Стек приложения

- Язык и рантайм: Go 1.25.
- API: RESTful JSON API, контракты из `01-analysis/api/` (доменные `api.yaml` + `models.yaml`, линтинг через `@redocly/cli`).
- HTTP: `net/http` + `chi` v5, middleware для request id, access log, panic recovery, JSON content type, Bearer token extraction.
- Аутентификация: Bearer-токен без JWT (хешированный session token, хранится в `auth_sessions`). Валидация токена — в сервисном слое через `ClientBySessionTokenHash`.
- БД: PostgreSQL 16, транзакции и row-level locks (`FOR UPDATE`) для бронирований, `pgx` v5 / `pgxpool`.
- Миграции: `goose` v3. Без ORM и кодогенерации SQL — все запросы пишутся вручную.
- Auth: phone/OTP flow, dev-реализация пишет OTP-код в лог (`slog`).
- Тесты: Go unit/integration tests; k6 performance tests — заготовки (директория `k6/` пуста).
- Runtime: Docker Compose (profiles: `db`, `app`, `migrations`, `k6`), конфигурация через env, structured logs через `slog`.

## Функционал и endpoints

| Домен | operationId | Method | Endpoint | Функционал |
|---|---|---|---|---|
| Auth | `requestOtp` | POST | `/auth/otp/request` | Запрос OTP по телефону |
| Auth | `verifyOtp` | POST | `/auth/otp/verify` | Проверка OTP, вход/регистрация, выдача токена |
| Profile | `getProfile` | GET | `/profile` | Профиль текущего клиента (телефон, аллергии, экипировка, лояльность) |
| Profile | `updateProfile` | PATCH | `/profile` | Обновление аллергий и собственной экипировки |
| Slots | `listSlots` | GET | `/slots?from=&to=&limit=&offset=` | Расписание классов с фильтром по дате, inline instructor |
| Slots | `getSlot` | GET | `/slots/{slotId}` | Карточка класса (меню, фото, сложность, цена, адрес, inline instructor) |
| Instructors | `getInstructor` | GET | `/instructors/{instructorId}` | Карточка шефа (имя, статус, рейтинг, специализация) |
| Bookings | `createBooking` | POST | `/bookings` (+ `Idempotency-Key`) | Атомарное создание брони, идемпотентность |
| Bookings | `listBookings` | GET | `/bookings?expand=slot&status=&limit=&offset=` | История броней клиента, inline slot при `expand=slot` |
| Bookings | `getBooking` | GET | `/bookings/{bookingId}` | Детали брони с inline slot + instructor |
| Bookings | `cancelBooking` | POST | `/bookings/{bookingId}/cancel` | Отмена брони (12h-правило, refundAmount) |
| Bookings | `transferBooking` | POST | `/bookings/{bookingId}/transfer` | Перенос брони на другой слот |
| Bookings | `upsertReview` | PUT | `/bookings/{bookingId}/review` | Оценка шефа (1–5) + опциональный текст, upsert |

Служебные endpoints: `GET /healthz`, `GET /readyz` (без аутентификации).

## Правила для реализации

- Любое изменение публичного API начинается с правки `01-analysis/api/*` и проверки `npm --prefix 01-analysis/api run lint`.
- Не добавлять instructor/admin UI/API, schedule CRUD, slot creation/editing, online payment, push-уведомления.

## Декомпозиция BE

### BE-00. Создать каркас backend-приложения

Сделано:
- `backend/` с Go module, `cmd/api/main.go`, `internal/config`, `internal/http`, `internal/domain`, `internal/storage`, `internal/service`.
- Makefile: `fmt` (gofmt), `lint` (go vet), `test` (go test), `run` (go run), `migrate` (goose).
- Docker Compose для PostgreSQL (`db`) и API (`app`), multi-stage Dockerfile.

Готово: `go test ./...` проходит, API стартует локально с `/healthz`.

### BE-01. OpenAPI-контракт как источник истины

Сделано:
- OpenAPI-спеки в `01-analysis/api/` разбиты по доменам (auth, slots, bookings, profile, instructors, common).
- Линтинг через `@redocly/cli` (`npm --prefix 01-analysis/api run lint`).
- Кодогенерация (`oapi-codegen`, `sqlc`) не используется — типы пишутся вручную, чтобы избежать расхождений и лишней сложности.

Отличия от изначального плана: без кодогенерации (ручной маппинг проще и точнее для текущего масштаба).

### BE-02. Реализовать общую HTTP-инфраструктуру

Сделано:
- Middleware (`internal/http/`): request ID (`X-Request-Id`), access log (structured JSON), panic recovery (500), JSON content type, Bearer token extraction.
- Единый формат ошибок: `{ code: string, message: string, details?: object }`.
- Кастомные 404/405 handlers возвращают JSON ошибки.
- Все доменные ручки завёрнуты в `/api/v1` (соответствует `servers.url` в OpenAPI-спеках).
- Служебные endpoints (`/healthz`, `/readyz`) — без префикса, без аутентификации.
- Коды ошибок (`internal/http/error.go`): `bad_request`, `unauthorized`, `forbidden`, `not_found`, `slot_full`, `double_booking`, `slot_cancelled`, `slot_started`, `already_cancelled`, `booking_not_active`, `booking_not_found`, `booking_not_completed`, `invalid_rating`, `invalid_code`, `idempotency_conflict`, `too_many_requests`, `internal_error`.

Готово: все handler-ы возвращают контрактные JSON-ошибки с правильными HTTP-статусами и machine-readable `code`.

### BE-03. Спроектировать БД и миграции

Сделано:
- Изначальная схема SUP-сёрфинга «Волна» (`00001_init.sql`, `00002_seed_dev.sql`): `clients`, `auth_sessions`, `otp_codes`, `routes`, `instructors`, `slots`, `bookings`, `idempotency_keys`.
- Миграция на кулинарные классы «Шеф-стол» (`00003_chef_table_transform.sql`):
  - `clients`: добавлены `allergies`, `own_equipment`, `loyalty_points`, `loyalty_status`.
  - `instructors`: добавлены `status` (Постоянный/Приглашённый), `rating` (1.0–5.0), `specialization`.
  - `slots`: заменены SUP-поля на `menu`, `difficulty`, `photo_urls`, `capacity` (1–12), `booked_count`, `price`, `address`, статусы `Активен`/`Отменён студией`.
  - `bookings`: заменены `seats_count`/`rental_count` на `equipment_type` (Своя/Прокат), добавлены `refund_amount`, `review_rating`, `review_text`.
  - Таблица `routes` удалена.
- Seed-данные для dev (`00004_seed_dev_chef.sql`): 3 шефа, 3 класса.
- Индексы: `phone`, `slot_id`, `client_id`, `start_at`, `status`, idempotency key, уникальный индекс активных броней.

Готово: миграции применяются на пустую PostgreSQL через `make migrate`.

### BE-04. Реализовать Auth: OTP и сессии

Сделано:
- `POST /auth/otp/request`: валидация телефона (`+[1-9]\d{1,14}`), rate limit (resend через 60с), OTP 6 цифр, TTL 5 минут, хеширование SHA-256.
- `POST /auth/otp/verify`: проверка OTP, авто-регистрация при первом входе, создание сессии (24h), возврат `{ accessToken, tokenType: "bearer", expiresIn: 86400 }`.
- Dev OTP provider: код пишется в лог + возвращается в ответе 202 (`message` содержит код).
- Нет эндпоинта `logout` — сессия живёт 24h или инвалидируется через `revoked_at`.

Отличия от изначального плана: без JWT (сырой session token с хешированием SHA-256), без logout, без смены телефона.

### BE-05. Реализовать Profile

Сделано:
- `GET /profile`: возвращает `{ id, phone, allergies, loyaltyPoints, loyaltyStatus, ownEquipment }`.
- `PATCH /profile`: обновляет `allergies` и `ownEquipment` (частичное обновление).
- Телефон — read-only, смена не поддерживается.
- Удаление аккаунта не поддерживается.

Отличия от изначального плана: без смены телефона, без удаления аккаунта.

### BE-06. Реализовать read-only каталог слотов и инструкторов

Сделано:
- `GET /slots`: фильтры `from`, `to` (формат `YYYY-MM-DD`), пагинация `limit`/`offset`. Сортировка по `start_at ASC`. Ответ: `{ items: Slot[], total: int }`.
- `GET /slots/{slotId}`: возвращает слот с inline `instructor` (имя, рейтинг, статус, специализация), `photoUrls`, `menu`, `difficulty`, `capacity`, `bookedCount`, `price`, `address`, `status`.
- `GET /instructors/{instructorId}`: справочник шефов (не используется мобильным приложением — instructor всегда inline в слот/бронь).

Отличия от изначального плана: без `route_type[]`, `instructor_id[]`, `only_available` фильтров. Без `listInstructors` эндпоинта. Instructor всегда inline в Slot (денормализация).

### BE-07. Реализовать атомарное создание брони

Сделано:
- `POST /bookings`: принимает `{ slotId, equipmentType }` + header `Idempotency-Key`.
- В одной транзакции:
  1. Блокировка `idempotency_keys` (`FOR UPDATE`) — проверка на повтор.
  2. Блокировка `slots` (`FOR UPDATE`) — проверка статуса, времени, свободных мест.
  3. Проверка на double booking (`client_id` + `slot_id` + status `Активна`).
  4. `booked_count += 1`.
  5. INSERT в `bookings`.
- `equipmentType` валидируется: `Своя` или `Прокат`.
- Идемпотентность: повторный запрос с тем же ключом и тем же телом возвращает ранее созданную бронь (без повторного создания).
- Идемпотентность с другим телом: `409 idempotency_conflict`.
- Ошибки: `409 slot_full`, `410 slot_cancelled`, `410 slot_started`, `409 double_booking`.

Отличия от изначального плана: `equipmentType` вместо `seats_count`/`rental_count`.

### BE-08. Реализовать список и детали броней

Сделано:
- `GET /bookings`: брони текущего клиента, фильтр `status`, пагинация `limit`/`offset`. При `?expand=slot` возвращает inline `Slot` с `Instructor` внутри.
- `GET /bookings/{bookingId}`: детали брони с inline `slot` + `instructor` (после правок от 2026-07-06).
- Проверка владельца: чужая бронь → `403 forbidden`, несуществующая → `404 booking_not_found`.
- JOIN `slots` и `instructors` во всех запросах — N+1 исключён.

### BE-09. Реализовать отмену, перенос и оценку

Сделано:
- `POST /bookings/{id}/cancel`:
  - В транзакции блокируется бронь и слот.
  - Правило 12 часов: `>12h` → бесплатная отмена (статус `ОтмененаКлиентом`, `booked_count -= 1`, `refund_amount = null`); `≤12h` → 50% возврат (`refund_amount = price * 0.5`, места не возвращаются).
  - Повторная отмена → `409 booking_not_active`.
- `POST /bookings/{id}/transfer`:
  - В транзакции: валидация старой и новой брони, атомарный перенос.
  - Новая бронь наследует `equipmentType` старой.
  - Ответ: `{ oldBooking: Booking, newBooking: Booking }`.
- `PUT /bookings/{id}/review`:
  - Только для завершённых броней (`status = Завершена`).
  - Rating 1–5, text опционально. Upsert (создать или обновить).
  - Пересчёт рейтинга шефа: `AVG(review_rating)` по всем оценкам.
  - Ответ: `{ review: { rating, text }, instructorRating: number }`.

Отличия от изначального плана: 12h-правило вместо 2h, добавлены transfer и review.

### BE-10. Довести контрактные ошибки и валидацию

Сделано:
- Machine-readable error codes во всех handler-ах: `bad_request`, `unauthorized`, `forbidden`, `not_found`, `slot_full`, `double_booking`, `slot_cancelled`, `slot_started`, `booking_not_active`, `booking_not_found`, `booking_not_completed`, `invalid_rating`, `invalid_code`, `idempotency_conflict`, `too_many_requests`, `internal_error`.
- Все ошибочные ответы возвращают `application/json` с телом `Error { code, message, details? }`.
- Валидация request body (`DisallowUnknownFields`), query params (даты, пагинация), path params.
- `401` на всех защищённых эндпоинтах при отсутствии/невалидном токене.
- Коды ошибок приведены в соответствие с API-спекой (правки от 2026-07-06: `booking_not_active`, `booking_not_completed`, `invalid_rating` через константы).

### BE-11. Добавить полный набор Go-тестов

Текущее состояние:
- Unit tests: `internal/config/config_test.go` — покрыт.
- HTTP handler tests, service tests, repository integration tests — отсутствуют.
- Concurrency/race tests для create/cancel — отсутствуют.
- Тестовая инфраструктура: `internal/storage/postgres/testutil/database.go` (изолированные схемы для integration tests).

Осталось: покрыть тестами handlers, services, repositories; добавить race/concurrency tests.

### BE-12. Добавить k6 performance tests до 300 concurrent users

Текущее состояние:
- Директории `k6/` и `cmd/k6seed/` существуют, но пусты.
- Docker Compose profile `k6` заготовлен.

Осталось: написать smoke-тест, сценарий конкурентного бронирования (300 VU), сценарий отмен.

### BE-13. Подготовить локальный запуск и документацию разработчика

Сделано:
- `backend/.env.example`, `backend/README.md`, `backend/Makefile`, `backend/compose.yaml`, `backend/Dockerfile`.
- Docker Compose profiles: `db`, `app`, `migrations`, `k6`.

Осталось: актуализировать README под проект «Шеф-стол» (было «Волна»).

### BE-14. Финальная проверка готовности BE

Сделано:
- `go build ./...` — проходит.
- `go vet ./...` — проходит.
- `go test ./...` — проходит.
- Все 13 endpoints из OpenAPI реализованы.
- Все endpoints покрывают требования `01-analysis/5-mobile-app-spec/` (13 operationId → 7 экранов + 4 bottom sheets).
- Контрактные ошибки соответствуют спецификации.
- Booking/cancel/transfer — атомарны (транзакции + `FOR UPDATE`).
- Inline instructor в Slot и Booking (денормализация, без N+1).
- `?expand=slot` в listBookings.
- `Idempotency-Key` в createBooking.
- 12h-правило отмены на бэкенде.
- Наследование `equipmentType` при transfer.
- Пересчёт `instructor.rating` при review.

Осталось: k6, полное покрытие тестами, актуализация README.

# API Rework Tasks — «Шеф-стол»

> Сгенерировано из `api-review.md`. Каждая задача — одна атомарная правка OpenAPI-спек.
> Порядок — по severity (Critical → Major → Minor → Info).

---

## Critical (обязательно исправить)

| Task | Ссылка на находку | Файл | Что сделать |
|------|-------------------|------|-------------|
| **T1** | F1, F12 | `bookings/api.yaml` | Добавить `GET /bookings/{bookingId}` с operationId `getBooking`, response `200` (`BookingResponse`), `404` (booking_not_found), `401` |
| **T2** | F4 | `bookings/api.yaml` | Добавить `404` (booking_not_found) в responses `POST /bookings/{bookingId}/cancel` |
| **T3** | F5 | `bookings/api.yaml` | Добавить `404` (booking_not_found) и `400` (invalid_rating) в responses `PUT /bookings/{bookingId}/review` |

---

## Major (желательно исправить)

| Task | Ссылка на находку | Файл | Что сделать |
|------|-------------------|------|-------------|
| **T4** | F7 | `bookings/api.yaml` | В `cancelBooking` и `transferBooking` заменить `410` → `409` для сценария booking_not_active (семантика: 409 — конфликт состояния, а не «ресурс удалён») |
| **T5** | F9 | `bookings/models.yaml` + `bookings/api.yaml` | Создать схему `ReviewResponse { review: { rating, text }, instructorRating: number }`. Заменить ответ `200` в upsertReview с `BookingResponse` на `ReviewResponse` |
| **T6** | F10 | `bookings/models.yaml` + `bookings/api.yaml` | Создать схему `TransferResponse { oldBooking: Booking, newBooking: Booking }`. Заменить ответ `200` в transferBooking с `BookingResponse` на `TransferResponse` |
| **T7** | F6 | `slots/api.yaml`, `bookings/api.yaml`, `profile/api.yaml`, `instructors/api.yaml` | Добавить `'401'` в `responses` всех защищённых эндпоинтов со ссылкой на `Error` и example `{ code: "unauthorized" }` |

---

## Minor / Info (опционально, вне текущего PR)

| Task | Ссылка на находку | Файл | Что сделать |
|------|-------------------|------|-------------|
| T8 | F3 | `common/models.yaml` | Добавить `instructor: { id, name, rating }` inline в `Slot` (денормализация для read-модели, убирает N+1 для SCR-002) |
| T9 | F11 | `bookings/api.yaml` | Добавить `?expand=slot` в `GET /bookings` с опциональным вложением `Slot` (убирает N+1 для SCR-005) |
| T10 | F8 | `use-cases.md` | Уточнить UC-2 шаг 3: «… (окончательное решение — на бэкенде)» |
| T11 | — | `auth/api.yaml` | Добавить `400` в `POST /auth/otp/request` (невалидный phone) |
| T12 | — | `auth/api.yaml` | Добавить `410` в `POST /auth/otp/verify` (код истёк) |
| T13 | — | `slots/api.yaml`, `profile/api.yaml` | Добавить `400` в `GET /slots` (невалидный date), `PATCH /profile` (невалидные поля) |

---

## Порядок выполнения

```
T1 → T2 → T3 → T4 → T5 → T6 → T7
(затем redocly lint)
```

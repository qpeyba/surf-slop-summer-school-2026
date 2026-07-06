# План реализации клиентского приложения «Шеф-стол»

> Детальные пошаговые спецификации лежат в `00-implementation-plan/`. Каждый шаг завершается smoke-тестом.

---

## TOC / Todo реализации

| Шаг | Название | Оценка |
|-----|---------|--------|
| [FE-00](00-implementation-plan/FE-00-project-init.md) | Инициализация проекта | 1 день |
| [FE-01](00-implementation-plan/FE-01-data-layer.md) | Слой данных (data): сеть, DTO, хранилище | 1 день |
| [FE-02](00-implementation-plan/FE-02-domain-layer.md) | Слой предметной области (domain) | 0.5 дня |
| [FE-03](00-implementation-plan/FE-03-di-navigation-theme.md) | DI + навигация + тема | 1 день |
| [FE-04](00-implementation-plan/FE-04-shared-components.md) | Shared UI-компоненты | 1.5 дня |
| [FE-05](00-implementation-plan/FE-05-auth-screen.md) | Экран Auth (SCR-006) | 1 день |
| [FE-06](00-implementation-plan/FE-06-schedule-filter.md) | Экран Schedule (SCR-001) + FilterDate (BS-004) | 1.5 дня |
| [FE-07](00-implementation-plan/FE-07-class-detail.md) | Экран ClassDetail (SCR-002) | 1 день |
| [FE-08](00-implementation-plan/FE-08-booking-form-success.md) | Экран BookingForm (SCR-003) + Success (SCR-004) | 1.5 дня |
| [FE-09](00-implementation-plan/FE-09-my-bookings.md) | Экран MyBookings (SCR-005) | 1 день |
| [FE-10](00-implementation-plan/FE-10-cancel-booking.md) | Bottom Sheet Cancel (BS-001) | 0.5 дня |
| [FE-11](00-implementation-plan/FE-11-transfer-booking.md) | Bottom Sheet Transfer (BS-002) | 1 день |
| [FE-12](00-implementation-plan/FE-12-rate-chef.md) | Bottom Sheet Rate (BS-003) | 0.5 дня |
| [FE-13](00-implementation-plan/FE-13-profile.md) | Экран Profile (SCR-007) | 1 день |
| [FE-14](00-implementation-plan/FE-14-fcm-push.md) | FCM Push-уведомления | 1 день |
| [FE-15](00-implementation-plan/FE-15-error-accessibility.md) | Обработка ошибок, edge-кейсы, accessibility | 1 день |
| [FE-16](00-implementation-plan/FE-16-testing.md) | Интеграционное тестирование и отладка | 1.5 дня |
| [FE-17](00-implementation-plan/FE-17-final-check.md) | Сборка и финальная проверка | 0.5 дня |

**Общая оценка:** ~16 рабочих дней для соло-разработчика.

---

## Матрица покрытия: экраны × API × use-case

| Экран | API endpoints | Use-case |
|-------|--------------|----------|
| SCR-006 Auth | `POST /auth/otp/request`, `POST /auth/otp/verify` | `RequestOtp`, `VerifyOtp` |
| SCR-001 Schedule | `GET /slots` | `GetSlots` |
| BS-004 FilterDate | — (клиентский фильтр) | — |
| SCR-002 ClassDetail | `GET /slots/{slotId}` | `GetSlotDetail` |
| SCR-003 BookingForm | `GET /slots/{slotId}`, `GET /profile`, `POST /bookings` | `GetSlotDetail`, `GetProfile`, `CreateBooking` |
| SCR-004 Success | `GET /bookings/{bookingId}` | `GetBookingDetail` |
| SCR-005 MyBookings | `GET /bookings?expand=slot` | `GetBookings` |
| BS-001 Cancel | `POST /bookings/{bookingId}/cancel` | `CancelBooking` |
| BS-002 Transfer | `GET /slots`, `POST /bookings/{bookingId}/transfer` | `GetSlots`, `TransferBooking` |
| BS-003 Rate | `PUT /bookings/{bookingId}/review` | `SubmitReview` |
| SCR-007 Profile | `GET /profile`, `PATCH /profile` | `GetProfile`, `UpdateProfile` |
| (не используется) | `GET /instructors/{instructorId}` | `GetInstructor` |

---

## Ключевые решения

1. **Idempotency-Key** — генерируется UUID на клиенте для каждого «Confirm Booking». Интерсептор добавляет заголовок только к `POST /bookings`. Повторный тап с тем же ключом вернёт ту же бронь.

2. **CancellationInfo** — client-side preview (для отображения правил до отмены) через `CancellationCalculator`, который вычисляет часы до начала слота и показывает вариант A/B. Реальное решение принимает бэкенд (время пересчитывается на сервере).

3. **12-часовое правило** — бэкенд сам пересчитывает `now()` на момент запроса `POST /cancel`. Клиент показывает preview, но окончательный `refundAmount` приходит в ответе API.

4. **Booking status filter** — `GET /bookings` по умолчанию возвращает все брони за 3 месяца. Группировка по статусам происходит на клиенте (для UX: «Активные (2)», «Завершённые (5)»).

5. **Pull-to-refresh** — на Schedule (SCR-001) и MyBookings (SCR-005). Используется material3 `pullToRefresh`.

6. **Офлайн** — отсутствует (NFR-7). Если сеть недоступна, показывается ErrorState с Retry.

7. **Loyalty status** — read-only, приходит с бэкенда в `GET /profile`. Прогресс-бар — клиентская декорация.

8. **`expand=slot`** — всегда передаётся при запросе `GET /bookings`, так как UI всегда показывает программу и шефа в карточке брони.

9. **Transfer flow** — 3 шага на клиенте. Шаг 2 (fullscreen выбор слота) переиспользует `GetSlotsUseCase` с тем же `DateStrip` + `ClassCard`, исключая текущий слот.

10. **Навигация по табам** — `Schedule`, `MyBookings`, `Profile` используют `saveState = true` / `restoreState = true` для сохранения состояния при переключении.

---

## Gradle-зависимости (ориентир)

```toml
[versions]
kotlin = "2.0.0"
compose-bom = "2024.06.00"
compose-navigation = "2.8.0"
hilt = "2.51.1"
hilt-navigation = "1.2.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinx-serialization = "1.7.0"
coil = "3.0.0"
security-crypto = "1.1.0-alpha06"
datastore = "1.1.1"
firebase-bom = "33.1.0"
lifecycle = "2.8.3"

[libraries]
# Compose BOM
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "compose-navigation" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Images
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }

# Storage
security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "security-crypto" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Firebase (FCM)
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase-bom" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }

# Testing
junit5 = { group = "org.junit.jupiter", name = "junit-jupiter", version = "5.10.0" }
mockk = { group = "io.mockk", name = "mockk", version = "1.13.11" }
turbine = { group = "app.cash.turbine", name = "turbine", version = "1.1.0" }
compose-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }

[plugins]
android-application = { id = "com.android.application", version = "8.4.0" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
```

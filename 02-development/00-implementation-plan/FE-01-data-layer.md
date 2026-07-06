# FE-01. Слой данных (data): сеть, DTO, хранилище

> Оценка: 1 день

## Задачи

- [ ] Все DTO в `data/remote/dto/request/` и `data/remote/dto/response/` (маппинг 1:1 к OpenAPI)
- [ ] Все 5 Retrofit-интерфейсов (`AuthApi`, `ProfileApi`, `SlotsApi`, `BookingsApi`, `InstructorsApi`)
- [ ] `AuthInterceptor` + `IdempotencyInterceptor` + `UnauthorizedInterceptor`
- [ ] `NetworkModule.kt` (Hilt): `@Singleton OkHttpClient`, `@Singleton Retrofit`, `@Provides` каждый API
- [ ] `TokenStorage.kt` (реализация через `EncryptedSharedPreferences`)
- [ ] `UserPreferences.kt` (DataStore)
- [ ] `FcmTokenManager.kt` — skeleton
- [ ] Все мапперы `dto → domain model`
- [ ] Все реализации репозиториев (`AuthRepositoryImpl`, `ProfileRepositoryImpl`, `SlotsRepositoryImpl`, `BookingsRepositoryImpl`, `InstructorsRepositoryImpl`)

## Маппинг API

Все 12 эндпоинтов — Retrofit-интерфейсы пишутся по OpenAPI-спеке из `01-analysis/api/`:

| Домен | Файл | Эндпоинты |
|-------|------|-----------|
| Auth | `AuthApi.kt` | `requestOtp`, `verifyOtp` |
| Profile | `ProfileApi.kt` | `getProfile`, `updateProfile` |
| Slots | `SlotsApi.kt` | `listSlots`, `getSlot` |
| Bookings | `BookingsApi.kt` | `createBooking`, `listBookings`, `getBooking`, `cancelBooking`, `transferBooking`, `upsertReview` |
| Instructors | `InstructorsApi.kt` | `getInstructor` |

## Критерий приёмки

- `Retrofit` создаётся без ошибок (проверка через Hilt compile)
- Все DTO успешно сериализуются/десериализуются через `kotlinx.serialization`
- `AuthInterceptor` добавляет `Authorization: Bearer` к запросам (проверяется через логи OkHttp)
- `IdempotencyInterceptor` добавляет `Idempotency-Key` только к `POST /bookings`
- Коды ошибок API (error.code) из OpenAPI замапплены на sealed-класс ошибок

# Выбор стека для клиентского приложения — «Шеф-стол»

> На основе анализа `01-analysis/`. Бэкенд: Go + REST API (JWT). Клиент: Android, с нуля.

---

## 1. Исходные технические ограничения (из NFR и API)

| # | Ограничение | Значение |
|---|-------------|----------|
| 1 | Платформа | **Только Android**. Распространение через Google Play. minSdk — уточняется |
| 2 | Стиль API | **Только REST** (5 доменов OpenAPI, 12 endpoints) |
| 3 | Авторизация | OTP по SMS → JWT Bearer-токен. Хранение: EncryptedSharedPreferences/Keystore |
| 4 | Push-уведомления | Firebase Cloud Messaging (FCM). Запрос `POST_NOTIFICATIONS` (Android 13+) |
| 5 | Дизайн-система | Material Design 3 (Material You). 412×915 dp эталон |
| 6 | Бизнес-логика | Вся на бэкенде. Клиент — «тонкий» потребитель API |
| 7 | Офлайн-режим | Отсутствует. Все данные — из API |
| 8 | Платёжная интеграция | Отсутствует в MVP (заглушка «Скоро: онлайн-оплата») |
| 9 | Роли | Только «Клиент» |
| 10 | Состояния экранов | 4 состояния: loading (skeleton), empty, error, success |

---

## Стек

### Kotlin + Jetpack Compose (нативный Android, современный)

```
Язык:         Kotlin 2.x
UI:           Jetpack Compose + Material 3
Навигация:    Compose Navigation (type-safe routes)
Сеть:         Retrofit + OkHttp + kotlinx.serialization (или Moshi)
DI:           Hilt (Dagger) или Koin
State:        ViewModel + StateFlow (или MVI через Orbit/Decompose)
Хранение:     AndroidX Security (EncryptedSharedPreferences) + DataStore
Push:         Firebase Cloud Messaging
Изображения:  Coil (Compose-native загрузка фото)
Календарь:    Material 3 DatePicker (встроенный) или кастомный
```

**Плюсы:**
- Полное соответствие NFR-2 (нативный Android)
- Современный декларативный UI, меньше boilerplate
- Полная поддержка Material 3 из коробки
- Единая экосистема: AndroidX, Google Play Services для FCM
- Самый богатый доступ к Android API: EncryptedSharedPreferences, Keystore, POST_NOTIFICATIONS

**Минусы:**
- Только Android (нет iOS, даже в перспективе)
- Compose — относительно молод; могут быть граничные баги
- Требует знания Kotlin + корутин + Flow
- Сборка тяжелее, чем у кроссплатформы (полный Android-тулчейн)

**Итог:** Максимальная нативность, полный доступ к Android API, минимум компромиссов. Идеально, если iOS не нужен ни сейчас, ни в перспективе 2–3 лет.
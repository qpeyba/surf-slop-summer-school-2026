# FE-14. FCM Push-уведомления

> Оценка: 1 день

## Задачи

- [ ] `FcmTokenManager.kt`: получение FCM токена (`FirebaseMessaging.getInstance().token`), сохранение в DataStore, обновление при смене
- [ ] `ChefTableFcmService.kt` (наследует `FirebaseMessagingService`):
  - `onNewToken`: сохранить новый токен
  - `onMessageReceived`:
    - Парсинг data payload:
      - `type`: `"reminder"` | `"cancel"`
      - `title`: заголовок уведомления
      - `body`: текст уведомления
      - `bookingId`: UUID брони
    - Создание `NotificationChannel` (при первом вызове):
      - ID: `"chef_table_reminders"`
      - Название: «Напоминания»
      - Важность: `IMPORTANCE_HIGH`
    - Построение и показ уведомления
    - PendingIntent при тапе: deep link на SCR-005 с фокусом на `bookingId`
- [ ] Запрос `POST_NOTIFICATIONS` (Android 13+, API 33+):
  - При первом запуске: `Manifest.permission.POST_NOTIFICATIONS` runtime request
  - Если отказано — показать объяснение и возможность открыть настройки
- [ ] Навигация при тапе на уведомление:
  - Intent extras → `bookingId`
  - `MainActivity.onCreate` / `onNewIntent`: если есть `bookingId` → навигация на SCR-005

## Маппинг API

*Не используется (FCM — внешний сервис)*

## Критерий приёмки

- При первом запуске: системный диалог запроса разрешения на уведомления
- FCM токен получен и сохранён (видно в логах)
- Notification channel «Напоминания» создан (видно в настройках Android → Приложения → Шеф-стол → Уведомления)
- При приходе push:
  - В foreground: уведомление не показывается (или показывается через NotificationCompat — уточнить поведение)
  - В background: системное уведомление с title и body
- Тап по уведомлению:
  - Открывается приложение на SCR-005
  - Список броней прокручивается к релевантной брони (по `bookingId`)
- При rotated FCM token: новый токен сохраняется в DataStore

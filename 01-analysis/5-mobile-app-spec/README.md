# Реестр экранов мобильного приложения — «Шеф-стол»

> Связывает экраны мобильного приложения с требованиями из `../2-requirements/`, UX-брифом из `../3-design-brief/`, моделью данных из `../4-design/data-model.md` и контрактами API из `../api/`.
> Каждый экран выводится из конкретных FR и UC. «Осиротевших» экранов нет.

---

## Дерево навигации

```
Tab Bar (только авторизованный пользователь)
├── [Расписание]   → SCR-001_Schedule
├── [Мои брони]    → SCR-005_MyBookings
└── [Профиль]      → SCR-007_Profile

Навигация по действиям пользователя:
├── SCR-001 → SCR-002_ClassDetail       (тап по карточке класса)
│   └── SCR-002 → SCR-003_BookingForm   (кнопка «Записаться»)
│       └── SCR-003 → SCR-004_BookingSuccess (успешное бронирование)
├── SCR-005 → BS-001_CancelConfirm      (кнопка «Отменить»)
├── SCR-005 → BS-002_TransferSelect     (кнопка «Перенести»)
├── SCR-005 → BS-003_RateChef           (кнопка «Оценить шефа»)
└── SCR-001 → BS-004_FilterDate         (фильтр дат)

Push-уведомления (LOGIC-006):
└── тап по уведомлению → SCR-005_MyBookings (активная/отменённая бронь)

Стартовый экран:
└── SCR-006_Auth (если не авторизован) → Tab Bar (после успеха)
```

---

## Реестр экранов

| ID | Файл | Название | Тип | FR | UC | US |
|----|------|----------|-----|----|----|-----|
| SCR-001 | [SCR-001_Schedule.md](SCR-001_Schedule.md) | Расписание | Экран | FR-1.1, FR-1.2, FR-1.3, FR-4.3 | UC-1 (1–2) | US-3 |
| SCR-002 | [SCR-002_ClassDetail.md](SCR-002_ClassDetail.md) | Карточка класса | Экран | FR-1.4, FR-3.1 | UC-1 (2) | US-4, US-5 |
| SCR-003 | [SCR-003_BookingForm.md](SCR-003_BookingForm.md) | Бронирование | Экран | FR-3.1, FR-3.2, FR-3.3 | UC-1 (4–5) | US-5, US-6 |
| SCR-004 | [SCR-004_BookingSuccess.md](SCR-004_BookingSuccess.md) | Успешная запись | Экран | FR-3.3 | UC-1 (8) | US-5 |
| SCR-005 | [SCR-005_MyBookings.md](SCR-005_MyBookings.md) | Мои брони | Экран | FR-2.4, FR-4.1, FR-4.2, FR-4.4 | UC-2, UC-3, UC-4, UC-5 | US-7, US-8, US-9, US-10 |
| SCR-006 | [SCR-006_Auth.md](SCR-006_Auth.md) | Авторизация | Экран | FR-2.1 | — | US-1 |
| SCR-007 | [SCR-007_Profile.md](SCR-007_Profile.md) | Профиль | Экран | FR-2.2, FR-2.3, FR-2.4 | — | US-2, US-7, US-11 |

## Реестр шторок / модальных окон

| ID | Файл | Название | Тип | FR | UC |
|----|------|----------|-----|----|-----|
| BS-001 | [BS-001_CancelConfirm.md](BS-001_CancelConfirm.md) | Отмена брони | Bottom Sheet | FR-4.1 | UC-2 |
| BS-002 | [BS-002_TransferSelect.md](BS-002_TransferSelect.md) | Перенос брони | Bottom Sheet | FR-4.2 | UC-3 |
| BS-003 | [BS-003_RateChef.md](BS-003_RateChef.md) | Оценка шефа | Bottom Sheet | FR-4.4 | UC-4 |
| BS-004 | [BS-004_FilterDate.md](BS-004_FilterDate.md) | Фильтр дат | Bottom Sheet | FR-1.2 | UC-1 |

---

## Реестр логик

| ID | Файл | Название | FR |
|----|------|----------|----|
| LOGIC-001 | [00_Логики/LOGIC-001_OTP-auth.md](00_Логики/LOGIC-001_OTP-auth.md) | OTP-авторизация | FR-2.1 |
| LOGIC-002 | [00_Логики/LOGIC-002_Доступность.md](00_Логики/LOGIC-002_Доступность.md) | Расчёт доступности | FR-3.1 |
| LOGIC-003 | [00_Логики/LOGIC-003_Цена_возврат.md](00_Логики/LOGIC-003_Цена_возврат.md) | Цена/возврат | FR-3.3, FR-4.1 |
| LOGIC-004 | [00_Логики/LOGIC-004_Правило_12ч_отмены.md](00_Логики/LOGIC-004_Правило_12ч_отмены.md) | Правило 12ч отмены | FR-4.1 |
| LOGIC-005 | [00_Логики/LOGIC-005_Фильтрация_по_дате.md](00_Логики/LOGIC-005_Фильтрация_по_дате.md) | Фильтрация по дате | FR-1.2 |
| LOGIC-006 | [00_Логики/LOGIC-006_Push.md](00_Логики/LOGIC-006_Push.md) | Push-уведомления | FR-5.1, FR-5.2 |
| LOGIC-007 | [00_Логики/LOGIC-007_Состояния.md](00_Логики/LOGIC-007_Состояния.md) | Паттерн состояний | — |

---

## Матрица FR → Экран

| FR | Экран(ы) |
|----|----------|
| FR-1.1 | SCR-001 |
| FR-1.2 | SCR-001, BS-004 |
| FR-1.3 | SCR-001 |
| FR-1.4 | SCR-002 |
| FR-2.1 | SCR-006 |
| FR-2.2 | SCR-007 |
| FR-2.3 | SCR-007 |
| FR-2.4 | SCR-005, SCR-007 |
| FR-3.1 | SCR-002, SCR-003 |
| FR-3.2 | SCR-003 |
| FR-3.3 | SCR-003, SCR-004 |
| FR-4.1 | SCR-005, BS-001 |
| FR-4.2 | SCR-005, BS-002 |
| FR-4.3 | SCR-001 |
| FR-4.4 | SCR-005, BS-003 |
| FR-5.1 | (система) → push → SCR-005 |
| FR-5.2 | (система) → push → SCR-005 |

## Матрица operationId → Экран

API-домены: [`auth/`](../api/auth/api.yaml), [`slots/`](../api/slots/api.yaml), [`bookings/`](../api/bookings/api.yaml), [`profile/`](../api/profile/api.yaml), [`instructors/`](../api/instructors/api.yaml)

| operationId | Домен | Экран(ы) |
|-------------|-------|----------|
| `requestOtp` | auth | SCR-006 |
| `verifyOtp` | auth | SCR-006 |
| `listSlots` | slots | SCR-001, BS-002, BS-004 |
| `getSlot` | slots | SCR-002, SCR-003, BS-001 |
| `createBooking` | bookings | SCR-003 |
| `listBookings` | bookings | SCR-005 |
| `getBooking` | bookings | BS-001, BS-002, BS-003 |
| `cancelBooking` | bookings | BS-001 |
| `transferBooking` | bookings | BS-002 |
| `upsertReview` | bookings | BS-003 |
| `getProfile` | profile | SCR-003, SCR-007 |
| `updateProfile` | profile | SCR-007 |
| `getInstructor` | instructors | (не используется — inline) |

---

## Соглашения
- **API-путь:** `../api/{domain}/api.yaml` → `{operationId}`
- **ID требований:** `FR-*`, `UC-*`, `US-*`, `NFR-*`
- **Только REST** — GraphQL отсутствует
- **Числа** (цена, лимиты, refundAmount) — из ответов API, не хардкод
- **Повторяемая логика** — ссылка на `LOGIC-XXX`, без дублирования
- **Каждый экран**: 4 состояния (loading/empty/error/success) + критерии приёмки

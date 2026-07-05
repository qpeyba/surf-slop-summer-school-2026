# Реестр экранов — «Шеф-стол»

> Связывает экраны мобильного приложения с требованиями из `../2-requirements/`.
> Каждый экран выводится из конкретных FR и UC. «Осиротевших» экранов нет.

---

## Карта навигации

```
Tab Bar
├── [Расписание]   → SCR-001_Schedule
├── [Мои брони]    → SCR-005_MyBookings
└── [Профиль]      → SCR-007_Profile

Переходы (push):
├── SCR-001 → SCR-002_ClassDetail      (тап по карточке класса)
│   └── SCR-002 → SCR-003_BookingForm   (кнопка «Записаться»)
│       └── SCR-003 → SCR-004_BookingSuccess (успешное бронирование)
├── SCR-005 → BS-001_CancelConfirm      (кнопка «Отменить»)
├── SCR-005 → BS-002_TransferSelect     (кнопка «Перенести»)
├── SCR-005 → BS-003_RateChef           (кнопка «Оценить шефа»)
└── SCR-001 → BS-004_FilterDate         (кнопка фильтра дат)

Стартовый экран:
└── SCR-006_Auth (если не авторизован) → Tab Bar
```

---

## Реестр экранов

| ID | Файл | Название | FR | UC | US |
|----|------|----------|----|----|-----|
| SCR-001 | [SCR-001_Schedule.md](SCR-001_Schedule.md) | Расписание | FR-1.1, FR-1.2, FR-1.3, FR-4.3 | UC-1 (шаги 1–2) | US-3 |
| SCR-002 | [SCR-002_ClassDetail.md](SCR-002_ClassDetail.md) | Карточка класса | FR-1.4, FR-3.1 | UC-1 (шаг 2) | US-4, US-5 |
| SCR-003 | [SCR-003_BookingForm.md](SCR-003_BookingForm.md) | Бронирование | FR-3.1, FR-3.2, FR-3.3 | UC-1 (шаги 4–5) | US-5, US-6 |
| SCR-004 | [SCR-004_BookingSuccess.md](SCR-004_BookingSuccess.md) | Успешная запись | FR-3.3 | UC-1 (шаг 8) | US-5 |
| SCR-005 | [SCR-005_MyBookings.md](SCR-005_MyBookings.md) | Мои брони | FR-2.4, FR-4.1, FR-4.2, FR-4.4 | UC-2, UC-3, UC-4, UC-5 | US-7, US-8, US-9, US-10 |
| SCR-006 | [SCR-006_Auth.md](SCR-006_Auth.md) | Авторизация | FR-2.1 | — | US-1 |
| SCR-007 | [SCR-007_Profile.md](SCR-007_Profile.md) | Профиль | FR-2.2, FR-2.3, FR-2.4 | — | US-2, US-7, US-11 |

## Реестр шторок / модальных окон

| ID | Файл | Название | FR | UC |
|----|------|----------|----|-----|
| BS-001 | [BS-001_CancelConfirm.md](BS-001_CancelConfirm.md) | Отмена брони | FR-4.1 | UC-2 |
| BS-002 | [BS-002_TransferSelect.md](BS-002_TransferSelect.md) | Перенос брони | FR-4.2 | UC-3 |
| BS-003 | [BS-003_RateChef.md](BS-003_RateChef.md) | Оценка шефа | FR-4.4 | UC-4 |
| BS-004 | [BS-004_FilterDate.md](BS-004_FilterDate.md) | Фильтр дат | FR-1.2 | UC-1 |

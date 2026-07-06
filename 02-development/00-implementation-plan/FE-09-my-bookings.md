# FE-09. Экран MyBookings (SCR-005)

> Оценка: 1 день

## Задачи

- [ ] `MyBookingsContract.kt`: `MyBookingsState` (bookings: UiState<List<BookingWithSlot>>, sections: Map<BookingStatus, List<BookingWithSlot>>, isRefreshing: Boolean), `MyBookingsEvent` (Refresh, CancelPressed, TransferPressed, RatePressed, BookingClicked)
- [ ] `MyBookingsViewModel.kt`:
  - При init: загрузка броней с `expand=slot`
  - Группировка по статусам: Active, Completed, Cancelled (CancelledByClient + CancelledByStudio + ClientNoShow)
  - Pull-to-refresh
  - Обработка всех 4 состояний
- [ ] `MyBookingsScreen.kt`:
  - `ChefTopAppBar` «Мои брони»
  - `LazyColumn` с pull-to-refresh, секции по статусам:
    - **Активные (n):** `BookingCard` с кнопками [Отменить] [Перенести]
    - **Завершённые (n):** `BookingCard` со звёздами (если есть ревью) или кнопкой [Оценить] (если нет) или [Изменить оценку] (если есть)
    - **Отменённые (n):** `BookingCard` со статусом + суммой возврата (если есть)
  - Тап по карточке → переход на детали?
  - Empty state: «У вас пока нет броней» + иллюстрация + кнопка «В расписание»
  - Вызов bottom sheets через state-флаги (`showCancelSheet`, `showTransferSheet`, `showRateSheet`)

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `GET /bookings?expand=slot` | `GetBookingsUseCase` |

## Критерий приёмки

- Группировка: секции с заголовками «Активные (2)», «Завершённые (5)», «Отменённые (1)»
- `BookingCard` для активных:
  - Фото + программа + дата/время + `ChefInfoRow`
  - `StatusBadge` «Активна» (синий)
  - Кнопки: [Отменить] (secondary) [Перенести] (primary)
- `BookingCard` для завершённых:
  - `StatusBadge` «Завершена» (зелёный)
  - `StarRatingDisplay` (если есть ревью)
  - Кнопка [Оценить шефа] (если нет ревью) или [Изменить оценку]
- `BookingCard` для отменённых:
  - `StatusBadge` «Отменена» (серый)
  - Сумма возврата: «Возврат: 1 750 ₽» (если `refundAmount != null`)
- Pull-to-refresh работает
- Empty state: иконка + текст + кнопка
- Loading: скелетоны `BookingCard`
- Error: сообщение + Retry

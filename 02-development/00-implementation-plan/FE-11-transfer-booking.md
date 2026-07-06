# FE-11. Bottom Sheet Transfer (BS-002)

> Оценка: 1 день

## Задачи

- [ ] `TransferBookingContract.kt`: `TransferBookingState` (step: Step1_SELECT | Step2_CONFIRM, oldBooking: BookingWithSlot, availableSlots: UiState<List<Slot>>, selectedSlot: Slot?, isProcessing: Boolean), `TransferBookingEvent` (SlotSelected, ConfirmPressed, BackPressed, DismissPressed)
- [ ] `TransferBookingViewModel.kt`:
  - Шаг 1: показ текущей брони
  - Переход к шагу 2: загрузка свободных слотов `GetSlotsUseCase` (тот же диапазон дат, что Schedule), исключить `oldBooking.slotId`
  - Шаг 3 (подтверждение): «Было / Будет» сравнение
  - `ConfirmPressed`: вызов `TransferBookingUseCase(bookingId, newSlotId)` → закрыть sheet → обновить список
- [ ] `TransferBookingSheet.kt`:
  - **Шаг 1 (sheet):** сводка текущей брони + кнопка «Выбрать новый слот»
  - **Шаг 2 (fullscreen):** `DateStrip` + `LazyColumn` из `ClassCard` (текущий слот исключён, только доступные слоты), тап по карточке → переход к шагу 3
  - **Шаг 3 (sheet):** сравнение «Было» (текущий слот) / «Будет» (новый слот) + кнопка «Перенести»

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `GET /slots?from=&to=` | `GetSlotsUseCase` |
| `POST /bookings/{bookingId}/transfer` | `TransferBookingUseCase` |

## Критерий приёмки

- Шаг 1: sheet с информацией о текущей брони + кнопка «Выбрать новый слот»
- Шаг 2 (fullscreen):
  - `DateStrip` на 7 дней
  - Список слотов: текущий слот исключён (`oldBooking.slotId` не отображается)
  - Только слоты со статусом «Активен» и `bookedCount < capacity`
  - Тап по карточке → переход к шагу 3
  - Кнопка «Назад» возвращает к шагу 1
- Шаг 3 (sheet):
  - «Было»: программа, дата, шеф текущей брони
  - «Будет»: программа, дата, шеф нового слота
  - Кнопка «Перенести» (с лоадером)
  - При успехе: sheet закрывается, список SCR-005 обновлён
  - При ошибке (slot_full, slot_cancelled) — сообщение в sheet
- Новая бронь наследует `equipmentType` старой (бэкенд обрабатывает)
- `TransferResponse` обрабатывается корректно (старая и новая бронь)

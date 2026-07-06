# FE-10. Bottom Sheet Cancel (BS-001)

> Оценка: 0.5 дня

## Задачи

- [ ] `CancelBookingContract.kt`: `CancelBookingState` (booking: BookingWithSlot, cancellationInfo: CancellationInfo, isProcessing: Boolean), `CancelBookingEvent` (ConfirmPressed, KeepPressed)
- [ ] `CancelBookingViewModel.kt`:
  - При открытии: `CancellationCalculator.calculate(booking.slot.dateTime)` — определение варианта A или B
  - `ConfirmPressed`: вызов `CancelBookingUseCase(bookingId)` → закрытие sheet + обновление списка
- [ ] `CancelBookingSheet.kt` (через `ModalBottomSheet`):
  - Handle
  - Заголовок «Отменить бронь?»
  - Сводка брони: программа, дата, время, шеф
  - Предупреждение:
    - **Вариант A (>12ч):** зелёная иконка ✓ + «Бесплатная отмена. Место вернётся в расписание.»
    - **Вариант B (≤12ч):** оранжевая иконка ⚠ + «50% возврат: {сумма} ₽. Возврат осуществляется владельцем студии вручную.»
    - **Слот уже начался:** красная иконка ✕ + «Невозможно отменить: класс уже начался.»
  - Кнопки: «Да, отменить» (destructive red) / «Оставить» (secondary)

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `POST /bookings/{bookingId}/cancel` | `CancelBookingUseCase` |

## Критерий приёмки

- Sheet открывается по тапу [Отменить] на карточке активной брони
- Вариант A: зелёная иконка, текст «Бесплатная отмена», кнопка «Да, отменить»
- Вариант B: оранжевое предупреждение, точная сумма возврата из `CancellationCalculator`, кнопка «Да, отменить»
- Слот начался: красная иконка, кнопка «Да, отменить» disabled, нельзя отменить
- После успешной отмены:
  - Sheet закрывается
  - Список броней обновляется (pull-to-refresh или optimistic update)
- При ошибке от API (409 booking_not_active) — сообщение в sheet
- Деструктивная кнопка красного цвета (destructive style)

# FE-08. Экран BookingForm (SCR-003) + Success (SCR-004)

> Оценка: 1.5 дня

## Задачи

- [ ] `BookingFormContract.kt`: `BookingFormState` (slot: Slot, equipment: EquipmentType, allergies: List<String>, totalPrice: Long, isSubmitting: Boolean, error: String?), `BookingFormEvent` (SelectEquipment, ConfirmPressed)
- [ ] `BookingFormViewModel.kt`:
  - При init: загрузка слота (`GetSlotDetailUseCase`) + профиля (`GetProfileUseCase`)
  - Предзаполнение `equipment` из профиля (`ownEquipment == true → Own`)
  - `ConfirmPressed`:
    - Генерация `IdempotencyKey`
    - Вызов `CreateBookingUseCase(slotId, equipment, idempotencyKey)`
    - Обработка успеха (201) → `Effect.ShowSuccess(bookingId)`
    - Обработка ошибок: `slot_full`, `slot_cancelled`, `slot_started`, `double_booking` — текст ошибки в state
- [ ] `BookingFormScreen.kt`:
  - Back-стрелка
  - Сводка класса: программа, дата/время, `ChefInfoRow`, `PriceTag`
  - `EquipmentSelector`: radio «Своя экипировка» / «Прокат»
  - Аллергии из профиля: `ChefChip`-ы (read-only) или текст «Не указаны»
  - Итого: `PriceTag`
  - Кнопка «Подтвердить бронирование» (с лоадером при отправке)
  - Заглушка «Скоро: онлайн-оплата» (disabled, серый)
  - Ошибка под кнопкой (если есть)
- [ ] `BookingSuccessContract.kt`, `BookingSuccessViewModel.kt`:
  - При init: загрузка брони по `bookingId` через `GetBookingDetailUseCase`
- [ ] `BookingSuccessScreen.kt`:
  - Центрированная иконка ✓ (зелёная)
  - Заголовок «Вы записаны!»
  - Карточка-сводка: программа, дата/время, шеф, адрес, оборудование, аллергии, цена
  - Текст: «Напоминания придут за 24ч, 3ч и 30 минут до начала»
  - Кнопки: «Мои брони» (таб MyBookings), «В расписание» (таб Schedule)

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `GET /slots/{slotId}` | `GetSlotDetailUseCase` |
| `GET /profile` | `GetProfileUseCase` |
| `POST /bookings` + `Idempotency-Key` | `CreateBookingUseCase` |
| `GET /bookings/{bookingId}` | `GetBookingDetailUseCase` |

## Критерий приёмки

- Сводка класса: программа, дата, шеф, цена — все поля корректны
- `EquipmentSelector`: radio-group, выбор меняет состояние, по умолчанию из профиля
- Аллергии: чипы (например «орехи», «глютен») или текст «Не указаны»
- Кнопка «Подтвердить»: лоадер при отправке, disabled во время запроса
- Заглушка «Скоро: онлайн-оплата» — всегда disabled
- При успехе: навигация на SCR-004 с очисткой BookingForm из back-stack
- SCR-004: полная сводка брони + две кнопки навигации
- При ошибке: текст ошибки под кнопкой (slot_full → «Мест больше нет», double_booking → «Вы уже записаны», slot_cancelled → «Класс отменён», slot_started → «Класс уже начался»)
- `Idempotency-Key` присутствует в заголовках запроса

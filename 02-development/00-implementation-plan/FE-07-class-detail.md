# FE-07. Экран ClassDetail (SCR-002)

> Оценка: 1 день

## Задачи

- [ ] `ClassDetailContract.kt`: `ClassDetailState` (slot: UiState<Slot>, isBookingAvailable: Boolean), `ClassDetailEvent` (BookPressed, BackPressed)
- [ ] `ClassDetailViewModel.kt`:
  - При init: загрузка слота по `slotId` через `GetSlotDetailUseCase`
  - Вычисление `isBookingAvailable`:
    - `status == Active`
    - `bookedCount < capacity`
    - `now() <= dateTime - 10 minutes`
- [ ] `ClassDetailScreen.kt`:
  - Back-стрелка в TopAppBar
  - `PhotoCarousel` (2:1 соотношение сторон, индикатор страниц)
  - Название программы + «от шефа {имя}»
  - Бейджи: `DifficultyBadge`, «3 часа»
  - Секция «Меню» (текст из `menu`, если есть)
  - Секция «Шеф»: `ChefInfoRow` — имя, `StarRatingDisplay`, специализация
  - `AddressRow` с иконкой локации
  - Дата и время (`dateTime` → читаемый формат)
  - `SpotsProgressBar`: «{bookedCount} из {capacity} мест»
  - `PriceTag`
  - Sticky CTA внизу: кнопка «Забронировать» / «Нет мест» / «Скоро начнётся»

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `GET /slots/{slotId}` | `GetSlotDetailUseCase` |

## Критерий приёмки

- `PhotoCarousel`: горизонтальный свайп, индикатор точек, загрузка через Coil
- Полная информация о классе: все поля из `SlotItem` отображены
- Inline instructor: имя, рейтинг (звёзды), статус (Постоянный/Приглашённый), специализация
- Кнопка CTA:
  - «Забронировать — {цена} ₽» (активна) — если слот доступен
  - «Нет мест» (disabled, серый) — если `bookedCount >= capacity`
  - «Скоро начнётся» (disabled) — если <10 минут до начала
  - «Отменён» (disabled) — если `status == "Отменён студией"`
- Loading: скелетон деталей (фото-плейсхолдер, текстовые строки)
- Error: сообщение + Retry

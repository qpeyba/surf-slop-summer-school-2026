# FE-12. Bottom Sheet Rate (BS-003)

> Оценка: 0.5 дня

## Задачи

- [ ] `RateChefContract.kt`: `RateChefState` (booking: BookingWithSlot, rating: Int, text: String, isSubmitting: Boolean, isEditing: Boolean), `RateChefEvent` (RatingChanged, TextChanged, SubmitPressed)
- [ ] `RateChefViewModel.kt`:
  - При открытии:
    - Если у брони есть `reviewRating` → режим редактирования (`isEditing = true`), предзаполнены rating и text
    - Если нет → режим создания
  - `SubmitPressed`: вызов `SubmitReviewUseCase(bookingId, rating, text)` → закрыть sheet → обновить список
  - Валидация: rating 1-5 обязательно, text ≤ 500 символов
- [ ] `RateChefSheet.kt` (через `ModalBottomSheet`):
  - Handle
  - Заголовок «Оценить шефа» или «Изменить оценку»
  - Информация о классе: программа, дата, `ChefInfoRow`
  - `StarRating` (интерактивная, 5 звёзд, tap заполняет слева направо)
  - Текстовая метка: «{n} из 5»
  - `OutlinedTextField`: «Поделитесь впечатлениями (необязательно)», максимум 500 символов, счётчик символов
  - Кнопка «Отправить» / «Сохранить»

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `PUT /bookings/{bookingId}/review` | `SubmitReviewUseCase` |

## Критерий приёмки

- Sheet открывается по тапу [Оценить шефа] (нет ревью) или [Изменить оценку] (есть ревью) на завершённой брони
- `StarRating`: интерактивные звёзды, tap на 3-ю → заполняются 1-3, tap на 1-ю → остаётся только 1-я
- Текстовая метка «{n} из 5» обновляется при изменении звёзд
- `OutlinedTextField`: placeholder «Поделитесь впечатлениями (необязательно)», счётчик «{n}/500»
- Кнопка «Отправить» в режиме создания, «Сохранить» в режиме редактирования
- Лоадер на кнопке при отправке
- При успехе:
  - Sheet закрывается
  - Рейтинг на карточке брони в SCR-005 обновляется
  - `instructorRating` из ответа API игнорируется (instructor приходит inline в следующих запросах)
- Ошибка (403 not_completed, 400 invalid_rating) — сообщение в sheet

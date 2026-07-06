# FE-06. Экран Schedule (SCR-001) + FilterDate (BS-004)

> Оценка: 1.5 дня

## Задачи

- [ ] `ScheduleContract.kt`: `ScheduleState` (classes: UiState<List<Slot>>, selectedDate: LocalDate, isRefreshing: Boolean), `ScheduleEvent` (DateSelected, Refresh, ClassClicked, OpenFilter)
- [ ] `ScheduleViewModel.kt`:
  - При init: загрузка слотов от `today` до `today + 7 дней`
  - Фильтр по дате: выбор даты → `from = selectedDate`, `to = selectedDate + 7 дней`
  - Pull-to-refresh: перезагрузка с тем же фильтром
  - Обработка всех 4 состояний (Loading, Empty, Error, Success)
- [ ] `ScheduleScreen.kt`:
  - `ChefTopAppBar` с заголовком «Шеф-стол» и иконкой фильтра
  - `DateStrip` — горизонтальная полоса дат, выбранная дата выделена
  - `LazyColumn` из `ClassCard` с pull-to-refresh (material3)
  - Пустой список: `EmptyState` — «Нет доступных классов» + иллюстрация
- [ ] `FilterDateContract.kt`, `FilterDateViewModel.kt`, `FilterDateSheet.kt` (BS-004):
  - Material 3 `DatePicker`
  - Quick-фильтры: «Сегодня», «Завтра», «Эта неделя», «Следующая неделя»
  - Кнопки «Сбросить» / «Применить»

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `GET /slots?from=&to=&limit=&offset=` | `GetSlotsUseCase` |

## Критерий приёмки

- По умолчанию: 7 дней от сегодня
- `DateStrip`: прокрутка, стрелки влево/вправо, сегодня выделено акцентом
- `ClassCard` содержит: фото, меню, `ChefInfoRow` (имя + звёзды), `DifficultyBadge`, `AddressRow`, время, `SpotsProgressBar`, `PriceTag`
- Pull-to-refresh: индикатор загрузки, обновление списка
- Empty state: иконка + «Нет доступных классов» + кнопка «Выбрать другую дату»
- Loading state: 3-4 скелетона `ClassCard`
- Error state: иконка ошибки + «Что-то пошло не так» + Retry
- BS-004: календарь открывается по тапу на иконку фильтра
- Слоты с `status == "Отменён студией"`: карточка затемнена, бейдж «Отменён», кнопка заблокирована

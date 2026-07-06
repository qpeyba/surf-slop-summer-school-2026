# FE-13. Экран Profile (SCR-007)

> Оценка: 1 день

## Задачи

- [ ] `ProfileContract.kt`: `ProfileState` (user: UiState<User>, allergiesInput: String, isUpdating: Boolean), `ProfileEvent` (AddAllergy, RemoveAllergy, EquipmentChanged, MyBookingsPressed, NotificationsToggled, LogoutPressed)
- [ ] `ProfileViewModel.kt`:
  - При init: `GetProfileUseCase` → state
  - `AddAllergy`: добавить в список, `PATCH /profile` с обновлённым списком
  - `RemoveAllergy`: удалить из списка, `PATCH /profile`
  - `EquipmentChanged`: переключить Own/Rental, `PATCH /profile` (авто-сохранение)
  - `LogoutPressed`: `LogoutUseCase` → очистка токена → навигация на Auth
- [ ] `ProfileScreen.kt`:
  - `ChefTopAppBar` «Профиль»
  - Аватар-заглушка (иконка человека в круге) + телефон (read-only)
  - **Блок лояльности:** «{points} баллов» + статус (Новичок / Серебро / Золото) + `LoyaltyProgressBar`
  - **Аллергии:** `FlowRow` из `ChefChip` (текст + крестик удалить), `OutlinedTextField` + кнопка «+ Добавить аллергию»
  - **Снаряжение по умолчанию:** `EquipmentSelector`, авто-сохранение при переключении
  - **Ссылки:**
    - «Мои брони» → переключение на таб MyBookings
    - «Уведомления» — `Switch` toggle (сохраняется в DataStore, заглушка в MVP)
  - Кнопка «Выйти» (destructive, снизу)

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `GET /profile` | `GetProfileUseCase` |
| `PATCH /profile` | `UpdateProfileUseCase` |

## Критерий приёмки

- Телефон: отображается в формате `+7 (XXX) XXX-XX-XX`, не редактируется
- Блок лояльности:
  - «{points} баллов»
  - Статус: «Новичок» / «Серебро» / «Золото»
  - `LoyaltyProgressBar` показывает прогресс до следующего уровня
- Аллергии:
  - Список чипов, каждый с крестиком удалить
  - Поле ввода + кнопка «+ Добавить»
  - При добавлении/удалении → `PATCH /profile` → обновление UI
- Снаряжение: radio Own/Rental, при переключении → авто-сохранение `PATCH /profile`
- «Мои брони» → переключение таба на MyBookings
- «Уведомления» — Switch, сохраняется в DataStore локально
- «Выйти» — диалог подтверждения? Или сразу очистка токена + навигация на Auth
- Loading: скелетон профиля
- Error: сообщение + Retry

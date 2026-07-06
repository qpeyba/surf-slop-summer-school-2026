# FE-03. DI + навигация + тема

> Оценка: 1 день

## Задачи

- [ ] `AppModule.kt`: `@Provides` DataStore, EncryptedSharedPreferences
- [ ] `RepositoryModule.kt`: bind всех 5 репозиториев
- [ ] `Routes.kt`: все сериализуемые route-объекты
- [ ] `Tabs.kt`: enum Schedule / MyBookings / Profile
- [ ] `ChefTableNavHost.kt`: граф навигации, условный `startDestination`
- [ ] `Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt`: Material 3 тема «Шеф-стол»

## Маппинг API

*Не используется*

## Критерий приёмки

- Приложение запускается с корректной темой (terracotta accent, фон `#FAFAFA`, карточки `#FFFFFF`, текст `#1A1A1A`/`#757575`)
- Навигация между пустыми экранами работает без ошибок (переходы, back-button)
- Tab bar отображается (Schedule | My Bookings | Profile), переключение табов работает с сохранением back-stack
- При отсутствии токена → стартовый экран Auth (SCR-006)
- При наличии токена → стартовый экран Schedule (SCR-001)

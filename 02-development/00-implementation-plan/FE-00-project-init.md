# FE-00. Инициализация проекта

> Оценка: 1 день

## Задачи

- [ ] Актуализировать `app/build.gradle.kts`: добавить Compose, Compose Navigation, Hilt, Retrofit, Coil, kotlinx.serialization, EncryptedSharedPreferences, DataStore, Firebase
- [ ] Настроить `libs.versions.toml` (version catalog)
- [ ] Создать пакетную структуру (папки) согласно `FE_ARCHITECTURE.md`, раздел 3
- [ ] Настроить `ChefTableApp.kt` с `@HiltAndroidApp`
- [ ] Настроить `MainActivity.kt` с `@AndroidEntryPoint`, `setContent { ChefTableTheme { ChefTableNavHost() } }`
- [ ] Добавить `google-services.json` в `app/` (FCM)

## Маппинг API

*Не используется*

## Критерий приёмки

- Проект собирается `./gradlew assembleDebug`
- Запускается пустой экран на эмуляторе/устройстве
- Hilt компилируется без ошибок

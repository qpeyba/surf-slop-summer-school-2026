# FE-05. Экран Auth (SCR-006)

> Оценка: 1 день

## Задачи

- [ ] `AuthContract.kt`: `AuthState` (step: PHONE | OTP, phone, error, isResendAvailable), `AuthEvent` (PhoneChanged, GetCodePressed, CodeChanged, ResendCodePressed)
- [ ] `AuthViewModel.kt`:
  - Валидация телефона: E.164, полные 10 цифр после маски
  - Вызов `RequestOtpUseCase` — обработка 202 (успех), 429 (rate limit), ошибка сети
  - Таймер resend: 60 секунд, обновление `isResendAvailable` и `resendSecondsLeft`
  - Вызов `VerifyOtpUseCase` — обработка 200 (сохранить токен, навигация), 401 invalid_code (очистить OTP, счётчик ошибок), 429 (блокировка 60s)
  - Максимум 3 ошибки OTP → блокировка ввода на 60s
- [ ] `AuthScreen.kt`:
  - Шаг PHONE: логотип + «Добро пожаловать!», `PhoneInput`, кнопка «Получить код» (disabled пока <10 цифр), согласие
  - Шаг OTP: «Код отправлен на ...», `OtpInput` (4 поля), `CountdownTimer`, error-текст под полями

## Маппинг API

| Endpoint | Use-case |
|----------|----------|
| `POST /auth/otp/request` | `RequestOtpUseCase` |
| `POST /auth/otp/verify` | `VerifyOtpUseCase` |

## Критерий приёмки

- Ввод телефона с маской `+7 (XXX) XXX-XX-XX`
- Кнопка «Получить код» активна только при 10 цифрах
- OTP-боксы: авто-переход между полями, авто-submit на 4-й цифре
- Таймер resend: «Отправить код повторно через 0:42»
- После 3 неверных попыток — блокировка на 60s с сообщением
- Успешный вход → токен сохранён → навигация на Schedule (Tab Bar) с очисткой back-stack
- 401 на любом экране → переход на Auth (через `UnauthorizedInterceptor`)

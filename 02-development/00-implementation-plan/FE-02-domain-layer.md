# FE-02. Слой предметной области (domain)

> Оценка: 0.5 дня

## Задачи

- [ ] Все domain-модели:
  - `User`, `Slot`, `Booking`, `BookingWithSlot`, `Instructor`, `Review`
  - `PaginatedResult`
  - `CancellationInfo`
- [ ] Все enum-ы:
  - `BookingStatus` (Active, CancelledByClient, CancelledByStudio, Completed, ClientNoShow)
  - `SlotStatus` (Active, CancelledByStudio)
  - `EquipmentType` (Own, Rental)
  - `Difficulty` (Beginner, Experienced)
  - `InstructorStatus` (Permanent, Guest)
  - `CancellationVariant` (A — >12h, B — ≤12h)
- [ ] Все интерфейсы репозиториев:
  - `AuthRepository`, `ProfileRepository`, `SlotsRepository`, `BookingsRepository`, `InstructorsRepository`
- [ ] Все use-case (14 шт.):
  - Auth: `RequestOtpUseCase`, `VerifyOtpUseCase`, `CheckAuthUseCase`, `LogoutUseCase`
  - Profile: `GetProfileUseCase`, `UpdateProfileUseCase`
  - Slots: `GetSlotsUseCase`, `GetSlotDetailUseCase`
  - Bookings: `CreateBookingUseCase`, `GetBookingsUseCase`, `GetBookingDetailUseCase`, `CancelBookingUseCase`, `TransferBookingUseCase`, `SubmitReviewUseCase`
  - Instructors: `GetInstructorUseCase`

## Маппинг API

*Доменный слой не зависит от API напрямую — use-case делегируют вызовы интерфейсам репозиториев.*

## Критерий приёмки

- Все use-case успешно инжектятся через Hilt (проверка компиляции)
- Все enum-ы имеют `fromApiValue(value: String)` companion-функцию
- `CancellationCalculator` (в `util/`) реализован и вычисляет variant A/B с точностью до часа
- Domain module компилируется без Android SDK (проверка: `./gradlew :domain:compileKotlin` если domain вынесен в отдельный модуль, или проверка что в domain-пакете нет `import android.*`)

# Архитектура клиентского приложения «Шеф-стол»

> На основе `01-analysis/stack.md`, API-контрактов и дизайн-спецификаций.
> Бэкенд: Go + REST API (JWT Bearer). Клиент: Android (Kotlin + Jetpack Compose).

---

## 1. Стек (утверждён)

| Слой | Технология |
|------|-----------|
| Язык | Kotlin 2.x |
| UI | Jetpack Compose + Material 3 (Material You) |
| Навигация | Compose Navigation (type-safe routes, сериализация через `kotlinx.serialization`) |
| Сеть | Retrofit 2 + OkHttp 4 + `kotlinx.serialization` (JSON) |
| DI | Hilt (Dagger) |
| State | ViewModel + `StateFlow<UiState>` (UDF) |
| Локальное хранение | AndroidX Security (`EncryptedSharedPreferences`) + DataStore |
| Изображения | Coil 3 (Compose-native) |
| Push | Firebase Cloud Messaging (FCM) |
| Календарь | Material 3 DatePicker + кастомный `DateStrip` |
| Тестирование | JUnit 5 + MockK + Turbine (flow testing) + Compose Testing |

---

## 2. Архитектурные принципы

1. **Clean Architecture (3 слоя):** `data → domain → ui`. Domain не зависит ни от чего. Data зависит только от domain. UI зависит от domain.
2. **UDF (Unidirectional Data Flow):** ViewModel выставляет `StateFlow<UiState>`, экраны потребляют state и посылают события через `onEvent()`.
3. **Тонкий клиент:** вся бизнес-логика на бэкенде. Клиент только отображает данные и валидирует ввод на уровне UI.
4. **4 состояния экрана:** `Loading` (скелетоны), `Empty` (доступные действия), `Error` (retry), `Success` (данные).
5. **Feature-based packaging:** каждый домен (auth, schedule, bookings, profile) — отдельный feature-пакет внутри `ui/`.
6. **Single Activity:** одна `MainActivity`, весь UI через Compose.

---

## 3. Структура папок

```
app/src/main/java/com/qpeyba/surf_slop_summer_school_2026/
│
├── ChefTableApp.kt                  # Application: инициализация Hilt, FCM
├── MainActivity.kt                  # Single Activity: setContent { ChefTableNavHost() }
│
├── di/                              # Hilt DI-модули
│   ├── AppModule.kt                 # DataStore, EncryptedSharedPreferences
│   ├── NetworkModule.kt             # OkHttpClient, Retrofit, AuthInterceptor
│   └── RepositoryModule.kt         # bind RepositoryImpl → Repository
│
├── data/                            # Data layer (реализация репозиториев, сеть, локальное хранение)
│   │
│   ├── remote/                      # Сеть (Retrofit)
│   │   ├── dto/                     # DTO (те же модели, что в OpenAPI)
│   │   │   ├── request/
│   │   │   │   ├── OtpRequest.kt            # { phone }
│   │   │   │   ├── OtpVerifyRequest.kt      # { phone, code }
│   │   │   │   ├── UpdateProfileRequest.kt   # { allergies?, ownEquipment? }
│   │   │   │   ├── CreateBookingRequest.kt   # { slotId, equipmentType }
│   │   │   │   ├── TransferRequest.kt        # { newSlotId }
│   │   │   │   └── ReviewRequest.kt          # { rating, text? }
│   │   │   └── response/
│   │   │       ├── TokenResponse.kt          # { accessToken, tokenType, expiresIn }
│   │   │       ├── ProfileResponse.kt        # { id, phone, allergies[], loyaltyPoints, loyaltyStatus, ownEquipment }
│   │   │       ├── SlotResponse.kt           # SlotItem: все поля из GET /slots
│   │   │       ├── SlotPageResponse.kt       # { items: SlotItem[], total }
│   │   │       ├── InstructorResponse.kt     # { id, name, status, rating, specialization }
│   │   │       ├── BookingResponse.kt        # BookingItem: все поля
│   │   │       ├── BookingPageResponse.kt    # { items: BookingItem[], total }
│   │   │       ├── BookingDetailResponse.kt  # BookingItem + inline Slot
│   │   │       ├── TransferResponse.kt       # { oldBooking, newBooking }
│   │   │       ├── ReviewResponse.kt         # { review, instructorRating }
│   │   │       └── ErrorResponse.kt          # { code, message, details? } — общий конверт ошибок
│   │   ├── api/                     # Retrofit-интерфейсы (5 доменов API)
│   │   │   ├── AuthApi.kt                  # requestOtp, verifyOtp
│   │   │   ├── ProfileApi.kt               # getProfile, updateProfile
│   │   │   ├── SlotsApi.kt                 # listSlots, getSlot
│   │   │   ├── BookingsApi.kt              # create/list/get/cancel/transfer/review
│   │   │   └── InstructorsApi.kt           # getInstructor
│   │   └── interceptor/
│   │       ├── AuthInterceptor.kt          # Добавляет Bearer токен из TokenStorage
│   │       ├── IdempotencyInterceptor.kt   # Генерирует UUID Idempotency-Key для createBooking
│   │       └── UnauthorizedInterceptor.kt  # Ловит 401 → clearToken → emit event навигации на SCR-006
│   │
│   ├── local/                       # Локальное хранение
│   │   ├── TokenStorage.kt                 # EncryptedSharedPreferences: read/write/clear accessToken
│   │   ├── UserPreferences.kt              # DataStore: фильтры, onboarding-флаги
│   │   └── FcmTokenManager.kt              # Сохранение/отправка FCM push-токена
│   │
│   ├── repository/                  # Реализации domain-репозиториев
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── ProfileRepositoryImpl.kt
│   │   ├── SlotsRepositoryImpl.kt
│   │   ├── BookingsRepositoryImpl.kt
│   │   └── InstructorsRepositoryImpl.kt
│   │
│   └── mapper/                      # Data-маппинг: DTO → domain model
│       ├── ProfileMapper.kt
│       ├── SlotMapper.kt
│       ├── BookingMapper.kt
│       └── InstructorMapper.kt
│
├── domain/                          # Domain layer (модели, use-case, интерфейсы репозиториев)
│   │
│   ├── model/                       # Бизнес-модели (не зависят от DTO)
│   │   ├── User.kt                  # id, phone, allergies, loyaltyPoints, loyaltyStatus, ownEquipment
│   │   ├── Slot.kt                  # id, dateTime, menu, photoUrls, difficulty, price, address, capacity, bookedCount, status, instructor
│   │   ├── Booking.kt               # id, slotId, equipmentType, status, refundAmount, reviewRating, reviewText, createdAt, cancelledAt
│   │   ├── BookingWithSlot.kt       # Booking + inline Slot
│   │   ├── Instructor.kt            # id, name, status, rating, specialization
│   │   ├── Review.kt                # rating (1-5), text?
│   │   ├── BookingStatus.kt         # Enum: Active, CancelledByClient, CancelledByStudio, Completed, ClientNoShow
│   │   ├── SlotStatus.kt            # Enum: Active, CancelledByStudio
│   │   ├── EquipmentType.kt         # Enum: Own, Rental
│   │   ├── Difficulty.kt            # Enum: Beginner, Experienced
│   │   ├── InstructorStatus.kt      # Enum: Permanent, Guest
│   │   ├── CancellationInfo.kt      # canCancel: Boolean, refundPercentage: Int?, refundAmount: Long?, warningText: String
│   │   └── PaginatedResult.kt       # items: List<T>, total: Int
│   │
│   ├── repository/                  # Интерфейсы (контракты) репозиториев
│   │   ├── AuthRepository.kt
│   │   ├── ProfileRepository.kt
│   │   ├── SlotsRepository.kt
│   │   ├── BookingsRepository.kt
│   │   └── InstructorsRepository.kt
│   │
│   └── usecase/                     # Use-case — один метод = одно действие
│       ├── auth/
│       │   ├── RequestOtpUseCase.kt         # validatePhone → api.requestOtp
│       │   ├── VerifyOtpUseCase.kt          # validatePhone+code → api.verifyOtp → tokenStorage.save
│       │   ├── CheckAuthUseCase.kt          # tokenStorage.hasValidToken
│       │   └── LogoutUseCase.kt             # tokenStorage.clear + fcmTokenManager.clear
│       ├── profile/
│       │   ├── GetProfileUseCase.kt         # api.getProfile → map
│       │   └── UpdateProfileUseCase.kt      # validate → api.updateProfile → map
│       ├── slots/
│       │   ├── GetSlotsUseCase.kt           # from, to, limit, offset → api.listSlots → map
│       │   └── GetSlotDetailUseCase.kt      # slotId → api.getSlot → map
│       ├── bookings/
│       │   ├── CreateBookingUseCase.kt      # slotId, equipmentType → api.createBooking
│       │   ├── GetBookingsUseCase.kt        # status, expand, limit, offset → api.listBookings → map
│       │   ├── GetBookingDetailUseCase.kt   # bookingId → api.getBooking → map
│       │   ├── CancelBookingUseCase.kt      # bookingId → api.cancelBooking + calc refund
│       │   ├── TransferBookingUseCase.kt    # bookingId, newSlotId → api.transferBooking
│       │   └── SubmitReviewUseCase.kt       # bookingId, rating, text → api.upsertReview
│       └── instructors/
│           └── GetInstructorUseCase.kt      # instructorId → api.getInstructor → map
│
├── ui/                              # Presentation layer
│   │
│   ├── navigation/                  # Навигация (Compose Navigation type-safe)
│   │   ├── Routes.kt                       # @Serializable sealed class/object для каждого экрана
│   │   ├── Tabs.kt                         # Enum: Schedule, MyBookings, Profile
│   │   └── ChefTableNavHost.kt             # NavHost(ScheduleScreen, ..., AuthScreen), conditional start
│   │
│   ├── theme/                       # Material 3 тема
│   │   ├── Color.kt                        # Terracotta, Olive, Cream, Dark — из дизайн-токенов
│   │   ├── Type.kt                         # Material 3 Typography (Roboto)
│   │   ├── Shape.kt                        # RoundedCornerShape(12.dp) карточки
│   │   └── Theme.kt                        # ChefTableTheme { MaterialTheme(...) }
│   │
│   ├── components/                  # Shared/reusable composable-элементы (дизайн-система)
│   │   ├── ChefTopAppBar.kt                # Верхний бар: заголовок + иконки (фильтр, назад)
│   │   ├── ChefBottomBar.kt                # Tab bar (Schedule | My Bookings | Profile)
│   │   ├── ChefButton.kt                   # Primary (accent), Secondary, Destructive (red) варианты
│   │   ├── ChefChip.kt                     # AllergyChip (текст + крестик удалить)
│   │   ├── PhoneInput.kt                   # Маскированный ввод: +7 (XXX) XXX-XX-XX
│   │   ├── OtpInput.kt                     # 4 бокса для OTP, авто-переход, авто-submit
│   │   ├── StarRating.kt                   # 5 звёзд (интерактивная, с текстовой меткой "4 из 5")
│   │   ├── StarRatingDisplay.kt            # 5 звёзд (неинтерактивная, только чтение)
│   │   ├── DateStrip.kt                    # Горизонтальная полоса 7 дат (сегодня → +6), прокрутка, стрелки
│   │   ├── PhotoCarousel.kt                # Горизонтальный pager изображений (Coil) с индикатором
│   │   ├── DifficultyBadge.kt              # Бейдж «Для новичков» / «Для опытных»
│   │   ├── StatusBadge.kt                  # Бейдж статуса (Активна, Отменена, Завершена)
│   │   ├── SpotsProgressBar.kt             # Линейный прогресс-бар: «3 из 12 мест»
│   │   ├── LoyaltyProgressBar.kt           # Прогресс-бар лояльности: «Серебро»
│   │   ├── ClassCard.kt                    # Карточка класса (для SCR-001): фото, меню, шеф, сложность, цена, места
│   │   ├── BookingCard.kt                  # Карточка брони (для SCR-005): программа, дата, шеф, статус, кнопки
│   │   ├── ChefInfoRow.kt                  # Строка с именем шефа + звёзды + количество отзывов
│   │   ├── AddressRow.kt                   # Адрес с иконкой
│   │   ├── EquipmentSelector.kt            # Radio-группа: «Своя экипировка» / «Прокат»
│   │   ├── PriceTag.kt                     # Цена с форматированием: «3 500 ₽»
│   │   ├── EmptyState.kt                   # Иконка-иллюстрация + текст + кнопка действия
│   │   ├── ErrorState.kt                   # Иконка ошибки + «Что-то пошло не так» + Retry
│   │   ├── LoadingSkeleton.kt              # Скелетоны (shimmer): карточки, детали
│   │   └── CountdownTimer.kt               # «Отправить код повторно через 0:42»
│   │
│   ├── screen/                      # Полноэкранные экраны (ViewModel + Composable)
│   │   ├── auth/
│   │   │   ├── AuthContract.kt              # UiState, Event (sealed), Effect (sealed)
│   │   │   ├── AuthViewModel.kt             # Шаг 1 (phone) → Шаг 2 (otp) → success → navigate
│   │   │   └── AuthScreen.kt                # Два состояния: ввод телефона / ввод OTP
│   │   ├── schedule/
│   │   │   ├── ScheduleContract.kt          # UiState: classes: List<Slot>, selectedDate, isLoading, error
│   │   │   ├── ScheduleViewModel.kt         # Загрузка слотов, фильтр по дате, pull-to-refresh
│   │   │   └── ScheduleScreen.kt            # DateStrip + LazyColumn из ClassCard
│   │   ├── classdetail/
│   │   │   ├── ClassDetailContract.kt       # UiState: slot, isLoading, error
│   │   │   ├── ClassDetailViewModel.kt      # Загрузка слота по slotId
│   │   │   └── ClassDetailScreen.kt         # PhotoCarousel + меню + шеф + адрес + места + CTA Book
│   │   ├── booking/
│   │   │   ├── BookingFormContract.kt       # UiState, Event (SelectEquipment, Confirm), Effect (ShowSuccess)
│   │   │   ├── BookingFormViewModel.kt      # Загрузка слота + профиля, валидация, отправка брони
│   │   │   └── BookingFormScreen.kt         # Сводка класса + оборудование + аллергии + цена + Confirm
│   │   ├── success/
│   │   │   ├── BookingSuccessContract.kt    # UiState: booking detail, reminder text
│   │   │   ├── BookingSuccessViewModel.kt   # Принимает bookingId, загружает детали
│   │   │   └── BookingSuccessScreen.kt      # Checkmark + «Вы записаны!» + сводка + кнопки
│   │   ├── mybookings/
│   │   │   ├── MyBookingsContract.kt        # UiState: sections (Active/Completed/Cancelled)
│   │   │   ├── MyBookingsViewModel.kt       # Загрузка броней с expand=slot, pull-to-refresh
│   │   │   └── MyBookingsScreen.kt          # LazyColumn сгруппирован по статусам, BookingCard
│   │   └── profile/
│   │       ├── ProfileContract.kt           # UiState: user, isLoading
│   │       ├── ProfileViewModel.kt          # Загрузка профиля, logout
│   │       └── ProfileScreen.kt             # Телефон, лояльность, аллергии, снаряжение, logout
│   │
│   └── sheet/                       # Bottom Sheets (ViewModel + Composable)
│       ├── cancel/
│       │   ├── CancelBookingContract.kt
│       │   ├── CancelBookingViewModel.kt    # Расчёт cancellation info (12h правило)
│       │   └── CancelBookingSheet.kt        # BS-001: сводка + предупреждение + кнопки
│       ├── transfer/
│       │   ├── TransferBookingContract.kt   # Шаги: Select → Confirm
│       │   ├── TransferBookingViewModel.kt  # Загрузка свободных слотов, выполнение переноса
│       │   └── TransferBookingSheet.kt      # BS-002: 3-шаговый flow (выбор слота → подтверждение)
│       ├── rate/
│       │   ├── RateChefContract.kt
│       │   ├── RateChefViewModel.kt         # Отправка / редактирование оценки
│       │   └── RateChefSheet.kt             # BS-003: звёзды + текст + Submit
│       └── filter/
│           ├── FilterDateContract.kt
│           ├── FilterDateViewModel.kt       # Состояние выбранной даты, quick-filters
│           └── FilterDateSheet.kt           # BS-004: календарь + Today/Tomorrow/This Week/Reset
│
├── util/                            # Утилиты (чистые функции, не зависят от Android SDK)
│   ├── UiState.kt                   # generic sealed class: Loading, Success<T>, Error(message), Empty
│   ├── PhoneMask.kt                 # Форматирование и валидация телефона
│   ├── DateFormatter.kt             # Форматирование дат для UI (ISO → локаль)
│   ├── PriceFormatter.kt            # Форматирование цены: 3500 → «3 500 ₽»
│   ├── CancellationCalculator.kt    # Расчёт cancellationInfo (12h правило, client-side preview)
│   ├── IdempotencyKey.kt            # UUID генератор
│   └── Constants.kt                 # Базовый URL, таймауты, константы дизайна (dp-сетка)
│
└── service/                         # Android-сервисы
    └── ChefTableFcmService.kt       # FirebaseMessagingService: приём push, парсинг, навигация на SCR-005
```

### Схема зависимостей

```
ui (screen, sheet, components)
  └── imports → domain (model, usecase, repository interface)
                  └── imports → data (repository impl, remote, local, mapper)
                                  └── imports → domain (model, repository interface)
```

- **Зелёная стрелка вверх:** ui → domain (use-case)
- **Зелёная стрелка вниз:** data реализует domain-интерфейсы (Dependency Inversion)
- **Domain не импортирует ничего из data и ui** (чистый Kotlin)
- **Ui не импортирует ничего из data** (только через domain)

---

## 4. Состояния экранов (UiState)

Каждый экран следует паттерну **MVI-lite** (ViewModel + sealed UiState):

```kotlin
// util/UiState.kt
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}
```

### Контракт экрана (MVI-подобный sealed Event)

```kotlin
// screen/schedule/ScheduleContract.kt (пример)
data class ScheduleState(
    val classes: UiState<List<Slot>>,
    val selectedDate: LocalDate,
    val isRefreshing: Boolean
)

sealed interface ScheduleEvent {
    data class DateSelected(val date: LocalDate) : ScheduleEvent
    data object Refresh : ScheduleEvent
    data class ClassClicked(val slotId: String) : ScheduleEvent
    data object OpenFilter : ScheduleEvent
}
```

### ViewModel (UDF)

```kotlin
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getSlots: GetSlotsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ScheduleState(ScheduleUiState.Loading, LocalDate.now(), false))
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    fun onEvent(event: ScheduleEvent) { /* reduce */ }
}
```

---

## 5. Слои в деталях

### 5.1. Data Layer (`data/`)

#### Remote (Retrofit)

Каждый домен API — отдельный Retrofit-интерфейс:

```kotlin
// data/remote/api/AuthApi.kt
interface AuthApi {
    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequest): BaseResponse<Unit>

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyRequest): BaseResponse<TokenResponse>
}
```

- **`BaseResponse<T>`** — обёртка для обработки ошибок с сервера. Содержит `T?` + `ErrorResponse?`.
- Все вызовы `suspend`, ошибки прокидываются через `Result<T>` или исключения c `ErrorResponse.code`.

#### Interceptors

| Interceptor | Задача |
|-------------|--------|
| `AuthInterceptor` | Добавляет `Authorization: Bearer <token>` из `TokenStorage` ко всем запросам кроме auth |
| `IdempotencyInterceptor` | Добавляет `Idempotency-Key: <UUID>` только к `POST /bookings` |
| `UnauthorizedInterceptor` | На 401 → `TokenStorage.clear()` → `EventBus.emit(ForceLogout)` → навигация на SCR-006 |

#### Local

```kotlin
// data/local/TokenStorage.kt
interface TokenStorage {
    suspend fun saveToken(token: String, expiresIn: Long)
    suspend fun getToken(): String?
    suspend fun hasValidToken(): Boolean
    suspend fun clear()
}
```

Реализация через `EncryptedSharedPreferences`.

#### Repository

Репозитории реализуют domain-интерфейсы, скрывают источник данных (remote/local) и маппят DTO → domain model:

```kotlin
// data/repository/SlotsRepositoryImpl.kt
class SlotsRepositoryImpl @Inject constructor(
    private val slotsApi: SlotsApi,
    private val slotMapper: SlotMapper
) : SlotsRepository {
    override suspend fun getSlots(from: LocalDate, to: LocalDate, limit: Int, offset: Int): PaginatedResult<Slot> {
        val response = slotsApi.listSlots(from.toString(), to.toString(), limit, offset)
        return PaginatedResult(
            items = response.items.map(slotMapper::toDomain),
            total = response.total
        )
    }
}
```

### 5.2. Domain Layer (`domain/`)

Только чистый Kotlin, никаких Android-зависимостей.

#### Use-case

Каждый use-case — ровно одно действие. Инжектятся напрямую в ViewModel:

```kotlin
// domain/usecase/bookings/CancelBookingUseCase.kt
class CancelBookingUseCase @Inject constructor(
    private val bookingsRepository: BookingsRepository
) {
    suspend operator fun invoke(bookingId: String): Result<Booking> {
        return bookingsRepository.cancelBooking(bookingId)
    }
}
```

#### Модели (enums)

```kotlin
enum class BookingStatus(val apiValue: String) {
    ACTIVE("Активна"),
    CANCELLED_BY_CLIENT("ОтмененаКлиентом"),
    CANCELLED_BY_STUDIO("ОтмененаСтудией"),
    COMPLETED("Завершена"),
    CLIENT_NO_SHOW("КлиентНеПришёл");

    companion object {
        fun fromApiValue(value: String): BookingStatus = entries.first { it.apiValue == value }
    }
}
```

Аналогично: `SlotStatus`, `EquipmentType`, `Difficulty`, `InstructorStatus`.

#### CancellationInfo

```kotlin
data class CancellationInfo(
    val canCancel: Boolean,          // false если слот уже начался
    val hoursRemaining: Double,      // оставшееся время до начала
    val refundPercentage: Int?,      // null = бесплатная, 50 = 50% возврат
    val refundAmount: Long?,         // сумма возврата в копейках/единицах
    val seatsReturned: Boolean,      // вернутся ли места в пул
    val warningText: String,         // Текст предупреждения для UI
    val variant: CancellationVariant  // A (>12h) / B (≤12h)
)
```

### 5.3. UI Layer (`ui/`)

#### Навигация (Compose Navigation type-safe)

```kotlin
// ui/navigation/Routes.kt
@Serializable sealed interface Route {
    @Serializable data object Auth : Route
    @Serializable data object Schedule : Route
    @Serializable data class ClassDetail(val slotId: String) : Route
    @Serializable data class BookingForm(val slotId: String) : Route
    @Serializable data class BookingSuccess(val bookingId: String) : Route
    @Serializable data object MyBookings : Route
    @Serializable data object Profile : Route
}
```

```kotlin
// ui/navigation/ChefTableNavHost.kt
@Composable
fun ChefTableNavHost(
    isAuthorized: Boolean,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        startDestination = if (isAuthorized) Route.Schedule else Route.Auth,
    ) {
        composable<Route.Auth> { AuthScreen(onLoginSuccess = { navController.navigate(Route.Schedule) { popUpTo(0) } }) }
        composable<Route.Schedule> { ScheduleScreen(onClassClick = { navController.navigate(Route.ClassDetail(it)) }) }
        composable<Route.ClassDetail> { ClassDetailScreen(onBook = { navController.navigate(Route.BookingForm(it)) }) }
        composable<Route.BookingForm> { BookingFormScreen(onSuccess = { navController.navigate(Route.BookingSuccess(it)) { popUpTo(Route.Schedule) } }) }
        composable<Route.BookingSuccess> { BookingSuccessScreen() }
        composable<Route.MyBookings> { MyBookingsScreen() }
        composable<Route.Profile> { ProfileScreen(onLogout = { navController.navigate(Route.Auth) { popUpTo(0) } }) }
    }
}
```

Tab bar (`Schedule` / `MyBookings` / `Profile`) — через `Scaffold(bottomBar = ChefBottomBar)`, где переключение табов не очищает back-stack каждого таба (используется `saveState` / `restoreState`).

#### Тема

```kotlin
// ui/theme/Color.kt
val Terracotta = Color(0xFF...)
val Olive = Color(0xFF...)
val Cream = Color(0xFF...)
val Surface = Color(0xFFFAFAFA)
val Card = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF757575)
```

```kotlin
// ui/theme/Theme.kt
@Composable
fun ChefTableTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Terracotta,
            background = Surface,
            surface = Card,
            onSurface = TextPrimary,
            // ...
        ),
        shapes = ChefTableShapes,
        typography = ChefTableTypography,
        content = content
    )
}
```

Дополнительные цвета (olive, destructive red) — через `ColorScheme` extension (добавлены в `Colors` через custom composable local или через `staticCompositionLocalOf`).

#### Компоненты

Все компоненты в `ui/components/` — stateless composable, принимающие состояние через параметры и вызывающие лямбды для действий. Пример:

```kotlin
@Composable
fun ClassCard(
    slot: Slot,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
fun EmptyState(
    illustration: ImageVector,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)

@Composable
fun LoadingSkeleton(variant: SkeletonVariant)  // SkeletonVariant.CARD, .DETAIL, .LIST
```

#### Экраны

Экран = `Contract` (State + Event) + `ViewModel` + Composable.

Composable экрана вызывает `viewModel.state.collectAsStateWithLifecycle()` и рендерит один из 4 UI-стейтов:

```kotlin
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel(), onClassClick: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val classes = state.classes) {
        is UiState.Loading -> LoadingSkeleton(SkeletonVariant.CARD)
        is UiState.Empty -> EmptyState(/* ... */)
        is UiState.Error -> ErrorState(message = classes.message, onRetry = { viewModel.onEvent(ScheduleEvent.Refresh) })
        is UiState.Success -> ScheduleContent(
            classes = classes.data,
            selectedDate = state.selectedDate,
            onDateSelected = { viewModel.onEvent(ScheduleEvent.DateSelected(it)) },
            onClassClick = onClassClick
        )
    }
}
```

#### Bottom Sheets

Каждый bottom sheet — это отдельный Composable, вызываемый через `ModalBottomSheet`:

```kotlin
// screen/mybookings/MyBookingsScreen.kt
if (state.showCancelSheet) {
    CancelBookingSheet(
        bookingId = state.selectedBookingId,
        booking = state.selectedBooking,
        onDismiss = { viewModel.onEvent(MyBookingsEvent.DismissSheet) },
        onConfirmed = { viewModel.onEvent(MyBookingsEvent.CancelConfirmed(it)) }
    )
}
```

Sheet-ы получают данные через собственный ViewModel или через параметры родительского экрана.

---

## 6. Поток аутентификации (SCR-006)

```
App start
  → CheckAuthUseCase (TokenStorage.hasValidToken)
    ├── true → Route.Schedule (Tab Bar)
    └── false → Route.Auth (SCR-006)

SCR-006:
  [Шаг 1: Телефон]
    → PhoneInput (маска +7 (XXX) XXX-XX-XX)
    → Button «Получить код» (активна при 10 цифрах)
    → RequestOtpUseCase(phone)
      ├── 202 → [Шаг 2: OTP]
      └── 429 → Показать «Подождите 1 минуту»

  [Шаг 2: OTP]
    → OtpInput (4 поля, авто-переход, авто-submit на 4-й цифре)
    → VerifyOtpUseCase(phone, code)
      ├── 200 → TokenStorage.save(token) → navigate(Route.Schedule) + popUpTo(0)
      ├── 401 invalid_code → Очистить поля + «Неверный код, попробуйте снова» (max 3 попытки)
      └── 429 too_many_requests → Заблокировать ввод на 60s
```

### Глобальная обработка 401

```
Любой API-запрос возвращает 401
  → UnauthorizedInterceptor.kt
    → TokenStorage.clear()
    → navigate(Route.Auth) + popUpTo(0)
    → Snackbar: «Сессия истекла, войдите снова»
```

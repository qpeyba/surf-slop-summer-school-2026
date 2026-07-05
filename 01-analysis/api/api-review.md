# API Review — «Шеф-стол»

> Диагностический отчёт. Проверка проводится по правилам из задания: файлы не исправляются, только фиксируются находки.
>
> Источники: `01-analysis/2-requirements/functional-requirements.md`, `use-cases.md`, `01-analysis/4-design/data-model.md`, `api-sequence.md`, `01-analysis/3-design-brief/README.md`, полное дерево `01-analysis/api/`.

---

## 1. Покрытие UC эндпоинтами

### Сводная таблица

| UC | Ключевой шаг | Эндпоинт | Статус |
|----|-------------|----------|--------|
| UC-1 (запись) | Расписание на 7 дней | `GET /slots` (listSlots) | ✅ |
| UC-1 (запись) | Карточка класса | `GET /slots/{slotId}` (getSlot) | ✅ |
| UC-1 (запись) | Подтягивание аллергий | `GET /profile` (getProfile) | ✅ |
| UC-1 (запись) | Создание брони | `POST /bookings` (createBooking) | ✅ |
| UC-2 (отмена) | Детали активной брони | `GET /bookings/{bookingId}` | ❌ **ОТСУТСТВУЕТ** |
| UC-2 (отмена) | Выполнение отмены | `POST /bookings/{bookingId}/cancel` (cancelBooking) | ✅ |
| UC-3 (перенос) | Детали активной брони | `GET /bookings/{bookingId}` | ❌ **ОТСУТСТВУЕТ** |
| UC-3 (перенос) | Список доступных слотов | `GET /slots` (listSlots) | ✅ |
| UC-3 (перенос) | Выполнение переноса | `POST /bookings/{bookingId}/transfer` (transferBooking) | ✅ |
| UC-4 (оценка) | Детали завершённой брони | `GET /bookings/{bookingId}` | ❌ **ОТСУТСТВУЕТ** |
| UC-4 (оценка) | Отправка/редактирование | `PUT /bookings/{bookingId}/review` (upsertReview) | ✅ |
| UC-5 (история) | Список броней | `GET /bookings` (listBookings) | ✅ |
| UC-1..UC-5 | Авторизация | `POST /auth/otp/request`, `POST /auth/otp/verify` | ✅ |

### F1 — `GET /bookings/{bookingId}` отсутствует в OpenAPI-спеке

- **Severity**: 🔴 Critical
- **Где**: `bookings/api.yaml` — нет path `/bookings/{bookingId}` с method `get`
- **Почему важно**: все три UC (2, 3, 4) и все диаграммы последовательностей (`api-sequence.md` §2.1, §3.1, §4.1) опираются на этот эндпоинт как **предварительный шаг** — клиент загружает бронь по `bookingId`, получает `slotId` и `status`, затем делает второй запрос за слотом. Без этого эндпоинта мобильное приложение не сможет отобразить карточку брони для отмены/переноса/оценки.
- **Как исправить**: добавить `GET /bookings/{bookingId}` в `bookings/api.yaml` с operationId `getBooking`, ответом `200` (`BookingResponse`) и `404` (booking not found).

---

## 2. operationId → экраны: покрытие и сиротство

| operationId | Экран(ы) | Нужен? |
|---|---|---|
| `requestOtp` | SCR-006_Auth | ✅ |
| `verifyOtp` | SCR-006_Auth | ✅ |
| `listSlots` | SCR-001_Schedule, BS-002_TransferSelect | ✅ |
| `getSlot` | SCR-002_ClassDetail | ✅ |
| `createBooking` | SCR-003_BookingForm | ✅ |
| `listBookings` | SCR-005_MyBookings | ✅ |
| `cancelBooking` | BS-001_CancelConfirm | ✅ |
| `transferBooking` | BS-002_TransferSelect | ✅ |
| `upsertReview` | BS-003_RateChef | ✅ |
| `getProfile` | SCR-007_Profile, SCR-003_BookingForm (аллергии) | ✅ |
| `updateProfile` | SCR-007_Profile | ✅ |
| `getInstructor` | SCR-002_ClassDetail (имя/рейтинг шефа) | ✅ |

### Находки

#### F2 — Нет осиротевших operationId

- **Severity**: ✅ Info (положительная находка)
- Все 12 перечисленных operationId востребованы хотя бы одним экраном. Ни один не является «мёртвым».

#### F3 — `getInstructor` формально нужен, но порождает N+2 для SCR-002

- **Severity**: 🟡 Minor
- **Где**: SCR-002_ClassDetail должен показать «имя и рейтинг шефа» (FR-1.4). Модель `Slot` (common/models.yaml:118) содержит только `instructorId` — без инлайн-объекта `Instructor`. Клиент вынужден делать второй вызов `GET /instructors/{instructorId}` после `GET /slots/{slotId}`.
- **Почему важно**: удваивает латентность загрузки карточки класса. Для списка (SCR-001) проблема умножается на число слотов, если на карточке расписания тоже показывается имя шефа.
- **Как исправить**: либо добавить `instructor: { id, name, rating }` inline в схему `Slot` (рекомендуется — это денормализация для read-модели), либо документировать двойной вызов как намеренный.

---

## 3. Отсутствующие HTTP-коды ошибок

### Сводка по эндпоинтам

| Эндпоинт | Есть | Отсутствует (нужно по UC / api-sequence) |
|---|---|---|
| `POST /auth/otp/request` | 202, 429 | 400 (невалидный phone) — Minor |
| `POST /auth/otp/verify` | 200, 409 | 400, 410 (код истёк) — Minor |
| `GET /slots` | 200 | 400, 401 — Minor |
| `GET /slots/{slotId}` | 200, 404 | 401 — Minor |
| `POST /bookings` | 201, 409, 410 | **401** (api-sequence.md явно фиксирует), 400 — Major |
| `GET /bookings` | 200 | 401 — Minor |
| `POST /bookings/{id}/cancel` | 200, 410 | **404** (api-seq. §2.2), **401** — Major |
| `POST /bookings/{id}/transfer` | 200, 409, 410 | 401, 404 — Major |
| `PUT /bookings/{id}/review` | 200, 403 | **404** (api-seq. §4.2), **400** (api-seq. §4.2) — Major |
| `GET /instructors/{id}` | 200, 404 | 401 — Minor |
| `GET /profile` | 200 | 401 — Minor |
| `PATCH /profile` | 200 | 400, 401 — Minor |

### F4 — `cancelBooking`: 404 (booking_not_found) задокументирован в api-sequence, но отсутствует в OpenAPI

- **Severity**: 🔴 Critical
- **Где**: `bookings/api.yaml:90-121` — cancelBooking: только 200 и 410
- **Что говорит api-sequence.md §2.2**: «`404 Not Found` — `bookingId` не существует → `{ error: "booking_not_found" }` → Сообщение «Бронь не найдена»».
- **Почему важно**: клиент обязан корректно обрабатывать случай несуществующего bookingId. Без задокументированного 404 фронтенд-разработчики не заложат этот сценарий — получится необработанный exception.
- **Как исправить**: добавить `'404'` в `responses` эндпоинта `POST /bookings/{bookingId}/cancel`.

### F5 — `upsertReview`: 404 и 400 задокументированы в api-sequence, но отсутствуют в OpenAPI

- **Severity**: 🔴 Critical
- **Где**: `bookings/api.yaml:177-214` — upsertReview: только 200 и 403
- **Что говорит api-sequence.md §4.2**: «`404 Not Found` — `bookingId` не существует» + «`400 Bad Request` — `rating` не в диапазоне 1–5».
- **Почему важно**: валидация rating на уровне API (400) и случай несуществующей брони (404) — обязательные сценарии.
- **Как исправить**: добавить `'404'` и `'400'` в `responses`.

### F6 — `createBooking` / все защищённые эндпоинты: 401 не объявлен явно

- **Severity**: 🟡 Major
- **Где**: все эндпоинты с `security: - Bearer: []` (bookings, slots, profile, instructors) не декларируют `401` в `responses`.
- **Что говорит api-sequence.md §1.2**: явно фиксирует `401 Unauthorized { error: "unauthorized" }` → «Редирект на экран авторизации».
- **Почему важно**: хотя OpenAPI допускает неявный 401 через security scheme, api-sequence.md явно документирует его как контракт. Отсутствие в спеке означает, что генераторы клиентского кода и документация не включат этот сценарий.
- **Как исправить**: добавить `'401'` в `responses` всех эндпоинтов с `security: - Bearer: []` со ссылкой на `common/models.yaml#/components/schemas/Error` и example `{ code: "unauthorized" }`.

### F7 — `cancelBooking` / `transferBooking`: расхождение 409 vs 410 для booking_not_active

- **Severity**: 🟡 Major
- **Где**:
  - `api-sequence.md` §2.2 (cancel) и §3.2 (transfer): статус «бронь не активна» → `409 Conflict`
  - `bookings/api.yaml`: тот же сценарий → `410 Gone`
- **Почему важно**: семантика разная.
  - `409 Conflict` = ресурс существует, но в конфликтующем состоянии → семантически верно (бронь есть, но статус ≠ Активна).
  - `410 Gone` = ресурс существовал, но удалён/недоступен навсегда → неверно (бронь не удалена, она просто в другом статусе).
- **Как исправить**: привести `bookings/api.yaml` в соответствие с `api-sequence.md` — использовать `409` для `booking_not_active`.

---

## 4. Дублирование серверной ответственности на клиенте

### Проверка критичных зон

| Зона | Где ответственность | Вердикт |
|---|---|---|
| Атомарность (bookedCount < capacity) | `createBooking` description: «Бэкенд атомарно проверяет…»; api-seq. §1.3 note 1; transferBooking description: «одной атомарной транзакцией». Клиент делает pre-check (UX) с оговоркой «не гарантия». | ✅ Корректное разделение |
| Правило 12ч / refundAmount | `cancelBooking` description: «Все расчёты и проверки выполняются на стороне бэкенда»; api-seq. §2.3 note 1: бэкенд пересчитывает оставшееся время самостоятельно. Клиент только показывает предупреждение. | ✅ Всё на сервере |
| Исполнение refund (деньги) | api-seq. §2.3 note 3: «refundAmount — только фиксация. Фактический возврат — ручной процесс владельца». | ✅ Не на клиенте |
| Пересчёт Instructor.rating | `upsertReview` description: «пересчитывает рейтинг шефа»; api-seq. §4.3 note 2: «Бэкенд пересчитывает как среднее арифметическое». Клиент только отправляет оценку. | ✅ Всё на сервере |
| Наследование equipmentType при переносе | `transferBooking` description: «Новая бронь наследует equipmentType от старой»; `TransferRequest` содержит только `newSlotId`. Клиент не отправляет equipmentType повторно. | ✅ Корректно |
| Идемпотентность | `createBooking` требует `Idempotency-Key` header (required). | ✅ |

### F8 — Формулировка UC-2 про расчёт времени на клиенте

- **Severity**: ℹ️ Info
- **Где**: `use-cases.md` строка 31: «Приложение рассчитывает время, оставшееся до начала класса.»
- **Почему важно**: формулировка создаёт ложное впечатление, что клиент отвечает за расчёт правила 12ч. На самом деле `api-sequence.md` §2.3 note 1 уточняет: это pre-check для предупреждения, финальный расчёт — на бэкенде. `bookings/api.yaml` (cancelBooking description) явно говорит «Все расчёты и проверки выполняются на стороне бэкенда».
- **Как исправить**: уточнить UC-2 шаг 3: «Приложение рассчитывает время для отображения предупреждения (окончательное решение — на бэкенде)».

---

## 5. Соответствие полей API схемам data-model.md

### CreateBookingRequest

| Поле | API (bookings/models.yaml) | data-model.md | Статус |
|---|---|---|---|
| `slotId` | ✅ string (uuid), required | Booking.slotId FK | ✅ |
| `equipmentType` | ✅ EquipmentType enum: «Своя» / «Прокат» | Booking.equipmentType | ✅ |
| `seats_count` | ❌ ОТСУТСТВУЕТ | ❌ Не предусмотрено моделью | ✅ Нет лишних полей |
| `rental_count` | ❌ ОТСУТСТВУЕТ | ❌ Не предусмотрено моделью | ✅ Нет лишних полей |

### Остальные схемы

| Схема API | Сущность data-model | Статус |
|---|---|---|
| `Slot` (common/models.yaml) | Slot (data-model §2.3) | ✅ Все 11 полей совпадают |
| `Booking` (common/models.yaml) | Booking (data-model §2.4) | ✅ Все 10 полей совпадают |
| `Client` (common/models.yaml) | Client (data-model §2.1) | ✅ Все 6 полей совпадают |
| `Instructor` (common/models.yaml) | Instructor (data-model §2.2) | ✅ Все 5 полей совпадают |
| `UpdateProfileRequest` (profile/models.yaml) | Client Write (data-model §4) | ✅ allergies + ownEquipment |

### F9 — `upsertReview` response: отсутствует `instructorRating`

- **Severity**: 🟡 Major
- **Где**: `bookings/api.yaml:200-205` — upsertReview 200 возвращает `BookingResponse`, т.е. `{ booking: Booking }`.
- **Что говорит api-sequence.md §4.2**: ответ должен содержать `{ review: { rating, text }, instructorRating: decimal }`. Поле `instructorRating` используется приложением для немедленного отображения обновлённого рейтинга шефа без дополнительного запроса (api-seq. §4.3 note 2).
- **Схема `Booking`** (`common/models.yaml:134-168`) не содержит поля `instructorRating`. Даже если бы содержала — рейтинг относится к Instructor, не к Booking.
- **Почему важно**: клиент будет вынужден делать дополнительный `GET /instructors/{instructorId}` после каждой оценки, чтобы получить актуальный рейтинг.
- **Как исправить**: либо расширить ответ upsertReview полем `instructorRating` (отдельная обёртка), либо добавить `instructorRating` в `BookingResponse` / `Booking`. Рекомендуется отдельная схема `ReviewResponse { review: { rating, text }, instructorRating: number }`.

### F10 — `transferBooking` response: расхождение с api-sequence.md

- **Severity**: 🟡 Major
- **Где**: `bookings/api.yaml:146-151` — transferBooking 200 возвращает `BookingResponse` (одну бронь — непонятно, старую или новую).
- **Что говорит api-sequence.md §3.2**: ответ должен содержать `{ oldBooking: { status: "Отменена клиентом" }, newBooking: { id, status: "Активна", slotId } }` — обе брони.
- **Почему важно**: клиенту нужно обновить в UI оба состояния — старую бронь пометить отменённой, новую показать активной. С одним объектом `BookingResponse` это невозможно.
- **Как исправить**: создать схему `TransferResponse { oldBooking: Booking, newBooking: Booking }` и использовать её в ответе 200.

---

## 6. Трассируемость API ↔ FR/UC

### Прямые связи (FR → endpoint)

| FR | Требование | Endpoint | Статус |
|----|-----------|----------|--------|
| FR-1.1 | Расписание на 7 дней | `GET /slots` (listSlots) | ✅ |
| FR-1.2 | Фильтр по дате | `GET /slots?from=&to=` | ✅ |
| FR-1.4 | Карточка класса (все атрибуты) | `GET /slots/{slotId}` + `GET /instructors/{id}` | ✅ (с оговоркой F3) |
| FR-2.1 | Вход по SMS | `POST /auth/otp/request` + `/verify` | ✅ |
| FR-2.2 | Аллергии (чтение/редакт.) | `GET /profile` + `PATCH /profile` | ✅ |
| FR-2.3 | Баланс + статус лояльности | `GET /profile` (read-only поля Client) | ✅ |
| FR-2.4 | История броней 3 мес. | `GET /bookings` + `GET /bookings/{id}` (отсутствует) | ⚠️ |
| FR-3.1 | Бронь (10 мин, места) | `POST /bookings` (createBooking) | ✅ |
| FR-3.2 | Экипировка + аллергии | `POST /bookings { equipmentType }`, `GET /profile` | ✅ |
| FR-3.3 | Итоговая стоимость + заглушка оплаты | `GET /slots/{slotId}` (price) | ✅ (заглушка — на клиенте) |
| FR-4.1 | Отмена (>12ч / ≤12ч) | `POST /bookings/{id}/cancel` | ✅ |
| FR-4.2 | Перенос брони | `POST /bookings/{id}/transfer` | ✅ |
| FR-4.3 | Блокировка отменённых студией | `GET /slots` / `GET /slots/{id}` (status) | ✅ |
| FR-4.4 | Оценка + редактирование | `PUT /bookings/{id}/review` | ✅ |
| FR-5.1 | Push-напоминания | Нет API (серверный push) | ✅ Вне скоупа |
| FR-5.2 | Push при отмене студией | Нет API (серверный push) | ✅ Вне скоупа |

### F11 — listBookings: N+1 проблема для UC-5

- **Severity**: 🟡 Minor
- **Где**: `GET /bookings` → `BookingPage` возвращает массив `Booking`, где каждый содержит только `slotId` (не слот целиком). Но UC-5 требует для каждой брони показывать: «дата и время класса, программа/меню, имя шефа, адрес студии» (use-cases.md:94).
- **Почему важно**: клиент вынужден для каждой брони делать `GET /slots/{slotId}` + `GET /instructors/{instructorId}`. Для списка из 10 броней это 1 + 10 + 10 = 21 запрос. При пустом списке — не страшно, но при заполненном — заметная задержка.
- **Как исправить**: либо добавить `?expand=slot` в `GET /bookings` с опциональным вложением `Slot` (и `Instructor` внутри `Slot`), либо принять как архитектурное решение с документированием N+1.

### F12 — FR-1.4 и FR-2.4: связь с отсутствующим `GET /bookings/{bookingId}`

- **Severity**: 🔴 Critical
- **Где**: FR-2.4 (история броней) подразумевает просмотр деталей конкретной брони; UC-5 шаги 3–5 (кнопки «Отменить», «Перенести», «Оценить») требуют перехода к карточке брони, которая загружается через `GET /bookings/{bookingId}`.
- **Почему важно**: без `GET /bookings/{bookingId}` цепочка взаимодействия разрывается: список броней есть, а детальный просмотр — нет.
- **Как исправить**: см. F1.

---

## Сводка находок

| # | Severity | Зона | Суть |
|---|----------|------|------|
| **F1** | 🔴 Critical | bookings/api.yaml | `GET /bookings/{bookingId}` отсутствует — нужен UC-2,3,4 и api-sequence.md |
| **F4** | 🔴 Critical | bookings/api.yaml | cancelBooking: нет `404` (booking_not_found) — задокументирован в api-seq. |
| **F5** | 🔴 Critical | bookings/api.yaml | upsertReview: нет `404` и `400` — задокументированы в api-seq. |
| **F12** | 🔴 Critical | Трассируемость | FR-2.4/UC-5 требуют `GET /bookings/{bookingId}`, он отсутствует |
| **F6** | 🟡 Major | Все домены | 401 не объявлен явно в responses защищённых эндпоинтов |
| **F7** | 🟡 Major | bookings/api.yaml | cancelBooking/transferBooking: api-seq. → `409`, OpenAPI → `410` для booking_not_active |
| **F9** | 🟡 Major | bookings/models.yaml | upsertReview response: нет `instructorRating` (есть в api-seq.) |
| **F10** | 🟡 Major | bookings/models.yaml | transferBooking response: один `BookingResponse` вместо `{oldBooking, newBooking}` |
| **F3** | 🟡 Minor | Slots / Instructors | Slot не содержит inline Instructor → N+1 для SCR-002 |
| **F11** | 🟡 Minor | Bookings | listBookings: Booking без slot-деталей → N+1 для SCR-005/UC-5 |
| **F8** | ℹ️ Info | use-cases.md | UC-2 формулировка «приложение рассчитывает время» — не уточнено, что pre-check |
| — | ℹ️ Info | auth/api.yaml | requestOtp: нет 400 (невалидный phone) |
| — | ℹ️ Info | auth/api.yaml | verifyOtp: нет 410 (код истёк) |

**Итого**: 4 Critical, 4 Major, 2 Minor, 3 Info.

---

## Не обнаружено

- Дублирования серверной ответственности на клиенте (атомарность, 12ч-правило, refund, рейтинг — все на бэкенде, клиент только pre-check для UX) ✅
- Лишних полей `seats_count` / `rental_count` в `CreateBookingRequest` ✅
- Осиротевших operationId (все 12 востребованы экранами) ✅
- Расхождений полей API-схем с data-model.md ✅
- Дублирования эндпоинтов между доменами ✅

# Баг-репорты «Шеф-стол»

Найдены при написании тестов и ревью кода.
Все три бага исправлены — см. соответствующие коммиты.

---

## BUG-1: MethodNotAllowed возвращает 404 вместо 405

| Поле | Значение |
|------|----------|
| **ID** | BUG-001 |
| **Серьёзность** | Low (Minor) |
| **Приоритет** | Low |
| **Найден** | Анализ кода `backend/internal/http/router.go:31-33` |
| **Статус** | Исправлен |
| **Компонент** | HTTP-роутер |

### Описание
Обработчик `MethodNotAllowed` возвращает `http.StatusNotFound` (404) и ошибку `CodeNotFound`, когда клиент использует правильный путь, но неправильный HTTP-метод. Должен возвращаться `http.StatusMethodNotAllowed` (405).

### Шаги воспроизведения
1. Отправить `POST /healthz` (существует только `GET /healthz`)
2. Получить ответ `404` с сообщением «Запрашиваемый ресурс не найден»

### Ожидаемое поведение
Должен вернуться `405 Method Not Allowed`.

### Фактическое поведение
Возвращается `404 Not Found`.

### Корень проблемы
`backend/internal/http/router.go:31-33`:
```go
router.MethodNotAllowed(func(w http.ResponseWriter, r *http.Request) {
    WriteError(w, http.StatusNotFound, CodeNotFound, "Запрашиваемый ресурс не найден.", nil)
})
```

### Исправление
```go
router.MethodNotAllowed(func(w http.ResponseWriter, r *http.Request) {
    WriteError(w, http.StatusMethodNotAllowed, CodeBadRequest, "Метод не поддерживается для данного ресурса.", nil)
})
```

---

## BUG-2: Transfer перезаписывает clientID — обход авторизации

| Поле | Значение |
|------|----------|
| **ID** | BUG-002 |
| **Серьёзность** | Critical (Безопасность) |
| **Приоритет** | High |
| **Найден** | Анализ кода `backend/internal/storage/postgres/bookings.go:350-354` |
| **Статус** | Исправлен |
| **Компонент** | Storage/Bookings.Transfer |

### Описание
Метод `Transfer` в `bookings.go` делает `Scan(&clientID, ...)` — перезаписывая `clientID` (идентификатор аутентифицированного пользователя) значением `client_id` из БД (владелец брони). В результате:
- Проверка владельца брони **не выполняется** (параметр `clientID` перезаписан значением из БД)
- Двойная бронь на новом слоте проверяется для **владельца**, а не для вызывающего
- Новая бронь создаётся для **владельца**, а не для вызывающего

**Любой авторизованный пользователь может перенести любую бронь на любой слот.**

### Шаги воспроизведения
1. Авторизоваться как Клиент A, создать бронь на Класс 1
2. Авторизоваться как Клиент B
3. Клиент B вызывает `POST /bookings/{bookingId_of_A}/transfer` с `newSlotId`
4. Бронь Клиента A переносится на новый слот — **от имени Клиента B**

### Ожидаемое поведение
Клиент B должен получить `403 Forbidden`.

### Фактическое поведение
Бронь Клиента A переносится. Новая бронь создаётся для Клиента A (но вызывал Клиент B).

### Корень проблемы
`backend/internal/storage/postgres/bookings.go:350-354`:
```go
err = tx.QueryRow(ctx, `
SELECT client_id::text, slot_id::text, equipment_type, status
FROM bookings
WHERE id = $1
FOR UPDATE`, bookingID).Scan(&clientID, &old.SlotID, &old.EquipmentType, &old.Status)
```
`clientID` — это параметр функции, идентифицирующий вызывающего. `Scan` перезаписывает его значением владельца брони.

### Исправление
Использовать отдельную переменную для `ownerID` и сравнивать с `clientID`:
```go
var ownerID string
err = tx.QueryRow(ctx, `
SELECT client_id::text, slot_id::text, equipment_type, status
FROM bookings
WHERE id = $1
FOR UPDATE`, bookingID).Scan(&ownerID, &old.SlotID, &old.EquipmentType, &old.Status)
if ownerID != clientID {
    return booking.Booking{}, booking.Booking{}, booking.ErrForbidden
}
```
И далее использовать `ownerID` (или `clientID`) для создания новой брони.

---

## BUG-3: Отмена ≤12ч не освобождает место (слот остаётся занятым)

| Поле | Значение |
|------|----------|
| **ID** | BUG-003 |
| **Серьёзность** | Major (Бизнес-логика) |
| **Приоритет** | High |
| **Найден** | Анализ кода `backend/internal/storage/postgres/bookings.go:316-323` |
| **Статус** | Исправлен |
| **Компонент** | Storage/Bookings.Cancel |

### Описание
При отмене брони ≤12 часов до начала класса: возврат 50% фиксируется, НО `booked_count` слота **не уменьшается**. Место остаётся занятым, другие клиенты не могут записаться. Это противоречит UC-2, который явно говорит «освобождает место».

### Шаги воспроизведения
1. Создать бронь на класс, до которого осталось ≤12 часов
2. Отменить бронь
3. Проверить `GET /slots/{slotId}` — `bookedCount` не уменьшился
4. Другой клиент пытается записаться — `409 slot_full`

### Ожидаемое поведение
При отмене в любой момент место должно освобождаться (`booked_count -= 1`). Разница только в `refundAmount` (>12ч — null, ≤12ч — 50%).

### Фактическое поведение
При ≤12ч `booked_count` не уменьшается. При >12ч — уменьшается.

### Корень проблемы
`backend/internal/storage/postgres/bookings.go:316-323`:
```go
if remaining > 12*time.Hour {
    if _, err := tx.Exec(ctx, `
UPDATE slots
SET booked_count = booked_count - 1
WHERE id = $1`, locked.SlotID); err != nil {
        return booking.Booking{}, fmt.Errorf("return slot booked count: %w", err)
    }
}
```
Условие `remaining > 12*time.Hour` исключает освобождение места при ≤12ч.

### Исправление
Убрать условие `if remaining > 12*time.Hour` — освобождать место всегда:
```go
if _, err := tx.Exec(ctx, `
UPDATE slots
SET booked_count = booked_count - 1
WHERE id = $1`, locked.SlotID); err != nil {
    return booking.Booking{}, fmt.Errorf("return slot booked count: %w", err)
}
```
А выше (где выставляется `refundAmount`) оставить условие ≤12ч для 50% возврата.

---

## Сводка

| ID | Серьёзность | Суть | Статус |
|----|-------------|------|--------|
| BUG-001 | Low | MethodNotAllowed → 404 вместо 405 | Исправлен |
| BUG-002 | Critical | Transfer обходит авторизацию | Исправлен |
| BUG-003 | Major | Отмена ≤12ч не освобождает место | Исправлен |

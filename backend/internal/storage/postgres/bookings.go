package postgres

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"time"

	"summer-school-2026/backend/internal/service/booking"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"
)

type BookingRepository struct {
	db *pgxpool.Pool
}

func NewBookingRepository(db *pgxpool.Pool) *BookingRepository {
	return &BookingRepository{db: db}
}

func (r *BookingRepository) ClientBySessionTokenHash(ctx context.Context, tokenHash string) (booking.Client, bool, error) {
	var client booking.Client
	err := r.db.QueryRow(ctx, `
SELECT c.id::text
FROM auth_sessions s
JOIN clients c ON c.id = s.client_id
WHERE s.token_hash = $1
  AND s.revoked_at IS NULL
  AND s.expires_at > now()
  AND c.deleted_at IS NULL`, tokenHash).Scan(&client.ID)
	if errors.Is(err, pgx.ErrNoRows) {
		return booking.Client{}, false, nil
	}
	if err != nil {
		return booking.Client{}, false, fmt.Errorf("query booking client by session: %w", err)
	}
	return client, true, nil
}

func (r *BookingRepository) Create(ctx context.Context, clientID string, command booking.CreateCommand, requestHash string, now time.Time) (booking.Booking, error) {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return booking.Booking{}, fmt.Errorf("begin create booking: %w", err)
	}
	defer tx.Rollback(ctx)

	if command.IdempotencyKey != "" {
		existing, ok, err := lockIdempotencyKey(ctx, tx, clientID, command.IdempotencyKey)
		if err != nil {
			return booking.Booking{}, err
		}
		if ok {
			if existing.RequestHash != requestHash {
				return booking.Booking{}, booking.ErrIdempotencyConflict
			}
			if existing.BookingID != "" {
				created, found, err := bookingByID(ctx, tx, existing.BookingID)
				if err != nil {
					return booking.Booking{}, err
				}
				if found {
					if err := tx.Commit(ctx); err != nil {
						return booking.Booking{}, fmt.Errorf("commit idempotent booking: %w", err)
					}
					return created, nil
				}
			}
		} else if err := insertIdempotencyKey(ctx, tx, clientID, command.IdempotencyKey, requestHash, now); err != nil {
			return booking.Booking{}, err
		}
	}

	slot, err := lockSlotForBooking(ctx, tx, command.SlotID)
	if err != nil {
		return booking.Booking{}, err
	}
	if slot.Status != "Активен" {
		return booking.Booking{}, booking.ErrSlotCancelled
	}
	if !now.Before(slot.DateTime) || slot.DateTime.Sub(now) < 10*time.Minute {
		return booking.Booking{}, booking.ErrSlotStarted
	}
	if slot.BookedCount >= slot.Capacity {
		return booking.Booking{}, booking.ErrSlotFull
	}

	var alreadyBooked bool
	if err := tx.QueryRow(ctx, `
SELECT EXISTS (
    SELECT 1 FROM bookings
    WHERE client_id = $1 AND slot_id = $2 AND status = 'Активна'
)`, clientID, command.SlotID).Scan(&alreadyBooked); err != nil {
		return booking.Booking{}, fmt.Errorf("check double booking: %w", err)
	}
	if alreadyBooked {
		return booking.Booking{}, booking.ErrDoubleBooking
	}

	if _, err := tx.Exec(ctx, `
UPDATE slots
SET booked_count = booked_count + 1
WHERE id = $1`, command.SlotID); err != nil {
		return booking.Booking{}, fmt.Errorf("update slot booked count: %w", err)
	}

	var bookingID string
	var createdAt time.Time
	err = tx.QueryRow(ctx, `
INSERT INTO bookings (slot_id, client_id, equipment_type, status, created_at)
VALUES ($1, $2, $3, 'Активна', $4)
RETURNING id::text, created_at`, command.SlotID, clientID, command.EquipmentType, now).Scan(&bookingID, &createdAt)
	if err != nil {
		if isUniqueViolation(err) {
			return booking.Booking{}, booking.ErrDoubleBooking
		}
		return booking.Booking{}, fmt.Errorf("insert booking: %w", err)
	}

	created, found, err := bookingByID(ctx, tx, bookingID)
	if err != nil {
		return booking.Booking{}, err
	}
	if !found {
		return booking.Booking{}, fmt.Errorf("created booking not found")
	}
	created.CreatedAt = createdAt

	if command.IdempotencyKey != "" {
		body, err := json.Marshal(map[string]string{"booking_id": bookingID})
		if err != nil {
			return booking.Booking{}, fmt.Errorf("marshal idempotency response: %w", err)
		}
		if _, err := tx.Exec(ctx, `
UPDATE idempotency_keys
SET response_status = 201, response_body = $4
WHERE client_id = $1 AND key = $2 AND request_hash = $3`, clientID, command.IdempotencyKey, requestHash, body); err != nil {
			return booking.Booking{}, fmt.Errorf("store idempotency response: %w", err)
		}
	}

	if err := tx.Commit(ctx); err != nil {
		return booking.Booking{}, fmt.Errorf("commit create booking: %w", err)
	}
	return created, nil
}

func (r *BookingRepository) List(ctx context.Context, clientID string, command booking.ListCommand) (booking.BookingList, error) {
	where := "WHERE b.client_id = $1"
	args := []any{clientID}
	if command.Status != nil {
		where += " AND b.status = $2"
		args = append(args, *command.Status)
	}

	var total int
	if err := r.db.QueryRow(ctx, `SELECT count(*) FROM bookings b `+where, args...).Scan(&total); err != nil {
		return booking.BookingList{}, fmt.Errorf("count bookings: %w", err)
	}

	queryArgs := append(args, command.Limit, command.Offset)
	rows, err := r.db.Query(ctx, bookingSelectSQL()+`
`+where+`
ORDER BY s.start_at DESC, b.created_at DESC
LIMIT $`+fmt.Sprint(len(args)+1)+` OFFSET $`+fmt.Sprint(len(args)+2), queryArgs...)
	if err != nil {
		return booking.BookingList{}, fmt.Errorf("query bookings: %w", err)
	}
	defer rows.Close()

	items := make([]booking.Booking, 0)
	for rows.Next() {
		item, err := scanBooking(rows)
		if err != nil {
			return booking.BookingList{}, err
		}
		items = append(items, item)
	}
	if err := rows.Err(); err != nil {
		return booking.BookingList{}, fmt.Errorf("iterate bookings: %w", err)
	}
	return booking.BookingList{Items: items, Total: total}, nil
}

func (r *BookingRepository) Get(ctx context.Context, clientID, bookingID string) (booking.Booking, error) {
	var ownerID string
	err := r.db.QueryRow(ctx, `SELECT client_id::text FROM bookings WHERE id = $1`, bookingID).Scan(&ownerID)
	if errors.Is(err, pgx.ErrNoRows) {
		return booking.Booking{}, booking.ErrNotFound
	}
	if err != nil {
		return booking.Booking{}, fmt.Errorf("query booking owner: %w", err)
	}
	if ownerID != clientID {
		return booking.Booking{}, booking.ErrForbidden
	}

	created, found, err := bookingByID(ctx, r.db, bookingID)
	if err != nil {
		return booking.Booking{}, err
	}
	if !found {
		return booking.Booking{}, booking.ErrNotFound
	}
	return created, nil
}

func (r *BookingRepository) Cancel(ctx context.Context, clientID, bookingID string, now time.Time) (booking.Booking, error) {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return booking.Booking{}, fmt.Errorf("begin cancel booking: %w", err)
	}
	defer tx.Rollback(ctx)

	var locked struct {
		OwnerID   string
		SlotID    string
		Status    string
		StartAt   time.Time
		Price     float64
	}
	err = tx.QueryRow(ctx, `
SELECT b.client_id::text, b.slot_id::text, b.status, s.start_at, s.price
FROM bookings b
JOIN slots s ON s.id = b.slot_id
WHERE b.id = $1
FOR UPDATE OF b, s`, bookingID).Scan(
		&locked.OwnerID,
		&locked.SlotID,
		&locked.Status,
		&locked.StartAt,
		&locked.Price,
	)
	if errors.Is(err, pgx.ErrNoRows) {
		return booking.Booking{}, booking.ErrNotFound
	}
	if err != nil {
		return booking.Booking{}, fmt.Errorf("lock booking for cancel: %w", err)
	}
	if locked.OwnerID != clientID {
		return booking.Booking{}, booking.ErrForbidden
	}
	if locked.Status != "Активна" {
		return booking.Booking{}, booking.ErrAlreadyCancelled
	}

	remaining := locked.StartAt.Sub(now)
	var refundAmount *float64
	if remaining <= 12*time.Hour {
		half := locked.Price * 0.5
		refundAmount = &half
	}

	if _, err := tx.Exec(ctx, `
UPDATE bookings
SET status = 'ОтмененаКлиентом', cancelled_at = $2, refund_amount = $3
WHERE id = $1`, bookingID, now, refundAmount); err != nil {
		return booking.Booking{}, fmt.Errorf("update booking cancel status: %w", err)
	}

	if remaining > 12*time.Hour {
		if _, err := tx.Exec(ctx, `
UPDATE slots
SET booked_count = booked_count - 1
WHERE id = $1`, locked.SlotID); err != nil {
			return booking.Booking{}, fmt.Errorf("return slot booked count: %w", err)
		}
	}

	cancelled, found, err := bookingByID(ctx, tx, bookingID)
	if err != nil {
		return booking.Booking{}, err
	}
	if !found {
		return booking.Booking{}, booking.ErrNotFound
	}
	if err := tx.Commit(ctx); err != nil {
		return booking.Booking{}, fmt.Errorf("commit cancel booking: %w", err)
	}
	return cancelled, nil
}

func (r *BookingRepository) Transfer(ctx context.Context, clientID, bookingID, newSlotID string, now time.Time) (booking.Booking, booking.Booking, error) {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return booking.Booking{}, booking.Booking{}, fmt.Errorf("begin transfer: %w", err)
	}
	defer tx.Rollback(ctx)

	var old struct {
		SlotID        string
		EquipmentType string
		Status        string
	}
	err = tx.QueryRow(ctx, `
SELECT client_id::text, slot_id::text, equipment_type, status
FROM bookings
WHERE id = $1
FOR UPDATE`, bookingID).Scan(&clientID, &old.SlotID, &old.EquipmentType, &old.Status)
	if errors.Is(err, pgx.ErrNoRows) {
		return booking.Booking{}, booking.Booking{}, booking.ErrNotFound
	}
	if err != nil {
		return booking.Booking{}, booking.Booking{}, fmt.Errorf("lock old booking: %w", err)
	}
	if old.Status != "Активна" {
		return booking.Booking{}, booking.Booking{}, booking.ErrAlreadyCancelled
	}
	if old.SlotID == newSlotID {
		return booking.Booking{}, booking.Booking{}, booking.ErrInvalidRequest
	}

	// Lock and validate new slot
	newSlot, err := lockSlotForBooking(ctx, tx, newSlotID)
	if err != nil {
		return booking.Booking{}, booking.Booking{}, err
	}
	if newSlot.Status != "Активен" {
		return booking.Booking{}, booking.Booking{}, booking.ErrSlotCancelled
	}
	if !now.Before(newSlot.DateTime) || newSlot.DateTime.Sub(now) < 10*time.Minute {
		return booking.Booking{}, booking.Booking{}, booking.ErrSlotStarted
	}
	if newSlot.BookedCount >= newSlot.Capacity {
		return booking.Booking{}, booking.Booking{}, booking.ErrSlotFull
	}

	var alreadyBooked bool
	if err := tx.QueryRow(ctx, `
SELECT EXISTS (
    SELECT 1 FROM bookings
    WHERE client_id = $1 AND slot_id = $2 AND status = 'Активна'
)`, clientID, newSlotID).Scan(&alreadyBooked); err != nil {
		return booking.Booking{}, booking.Booking{}, fmt.Errorf("check double booking: %w", err)
	}
	if alreadyBooked {
		return booking.Booking{}, booking.Booking{}, booking.ErrDoubleBooking
	}

	// Cancel old booking
	if _, err := tx.Exec(ctx, `
UPDATE bookings
SET status = 'ОтмененаКлиентом', cancelled_at = $2
WHERE id = $1`, bookingID, now); err != nil {
		return booking.Booking{}, booking.Booking{}, fmt.Errorf("cancel old booking: %w", err)
	}
	if _, err := tx.Exec(ctx, `
UPDATE slots SET booked_count = booked_count - 1 WHERE id = $1`, old.SlotID); err != nil {
		return booking.Booking{}, booking.Booking{}, fmt.Errorf("return old slot: %w", err)
	}

	// Create new booking
	if _, err := tx.Exec(ctx, `
UPDATE slots SET booked_count = booked_count + 1 WHERE id = $1`, newSlotID); err != nil {
		return booking.Booking{}, booking.Booking{}, fmt.Errorf("book new slot: %w", err)
	}

	var newBookingID string
	err = tx.QueryRow(ctx, `
INSERT INTO bookings (slot_id, client_id, equipment_type, status, created_at)
VALUES ($1, $2, $3, 'Активна', $4)
RETURNING id::text`, newSlotID, clientID, old.EquipmentType, now).Scan(&newBookingID)
	if err != nil {
		return booking.Booking{}, booking.Booking{}, fmt.Errorf("insert new booking: %w", err)
	}

	oldBooking, _, err := bookingByID(ctx, tx, bookingID)
	if err != nil {
		return booking.Booking{}, booking.Booking{}, err
	}
	newBooking, _, err := bookingByID(ctx, tx, newBookingID)
	if err != nil {
		return booking.Booking{}, booking.Booking{}, err
	}

	if err := tx.Commit(ctx); err != nil {
		return booking.Booking{}, booking.Booking{}, fmt.Errorf("commit transfer: %w", err)
	}
	return oldBooking, newBooking, nil
}

func (r *BookingRepository) UpsertReview(ctx context.Context, clientID, bookingID string, rating int, text *string) (booking.ReviewResult, error) {
	var status string
	var instructorID string
	err := r.db.QueryRow(ctx, `
SELECT b.status, s.instructor_id::text
FROM bookings b
JOIN slots s ON s.id = b.slot_id
WHERE b.id = $1 AND b.client_id = $2`, bookingID, clientID).Scan(&status, &instructorID)
	if errors.Is(err, pgx.ErrNoRows) {
		return booking.ReviewResult{}, booking.ErrNotFound
	}
	if err != nil {
		return booking.ReviewResult{}, fmt.Errorf("check booking for review: %w", err)
	}
	if status != "Завершена" {
		return booking.ReviewResult{}, booking.ErrNotCompleted
	}

	if _, err := r.db.Exec(ctx, `
UPDATE bookings
SET review_rating = $3, review_text = $4
WHERE id = $1 AND client_id = $2`, bookingID, clientID, rating, text); err != nil {
		return booking.ReviewResult{}, fmt.Errorf("upsert review: %w", err)
	}

	var newRating float64
	if err := r.db.QueryRow(ctx, `
UPDATE instructors
SET rating = (SELECT COALESCE(AVG(b.review_rating)::numeric(2,1), 5.0)
              FROM bookings b
              JOIN slots s2 ON s2.id = b.slot_id
              WHERE s2.instructor_id = $1 AND b.review_rating IS NOT NULL)
WHERE id = $1
RETURNING rating`, instructorID).Scan(&newRating); err != nil {
		return booking.ReviewResult{}, fmt.Errorf("recalc instructor rating: %w", err)
	}

	return booking.ReviewResult{Review: booking.Review{Rating: rating, Text: text}, InstructorRating: newRating}, nil
}

// ---------- helpers ----------

type idempotencyRecord struct {
	RequestHash string
	BookingID   string
}

func lockIdempotencyKey(ctx context.Context, tx pgx.Tx, clientID, key string) (idempotencyRecord, bool, error) {
	var record idempotencyRecord
	var body []byte
	err := tx.QueryRow(ctx, `
SELECT request_hash, response_body
FROM idempotency_keys
WHERE client_id = $1 AND key = $2
FOR UPDATE`, clientID, key).Scan(&record.RequestHash, &body)
	if errors.Is(err, pgx.ErrNoRows) {
		return idempotencyRecord{}, false, nil
	}
	if err != nil {
		return idempotencyRecord{}, false, fmt.Errorf("lock idempotency key: %w", err)
	}
	if len(body) > 0 {
		var payload struct {
			BookingID string `json:"booking_id"`
		}
		if err := json.Unmarshal(body, &payload); err != nil {
			return idempotencyRecord{}, false, fmt.Errorf("unmarshal idempotency response: %w", err)
		}
		record.BookingID = payload.BookingID
	}
	return record, true, nil
}

func insertIdempotencyKey(ctx context.Context, tx pgx.Tx, clientID, key, requestHash string, now time.Time) error {
	_, err := tx.Exec(ctx, `
INSERT INTO idempotency_keys (client_id, key, request_hash, expires_at)
VALUES ($1, $2, $3, $4)`, clientID, key, requestHash, now.Add(24*time.Hour))
	if err != nil {
		return fmt.Errorf("insert idempotency key: %w", err)
	}
	return nil
}

type lockSlotRow struct {
	DateTime    time.Time
	Status      string
	Capacity    int
	BookedCount int
}

func lockSlotForBooking(ctx context.Context, tx pgx.Tx, slotID string) (lockSlotRow, error) {
	var slot lockSlotRow
	err := tx.QueryRow(ctx, `
SELECT start_at, status, capacity, booked_count
FROM slots
WHERE id = $1
FOR UPDATE`, slotID).Scan(&slot.DateTime, &slot.Status, &slot.Capacity, &slot.BookedCount)
	if errors.Is(err, pgx.ErrNoRows) {
		return lockSlotRow{}, booking.ErrSlotStarted
	}
	if err != nil {
		return lockSlotRow{}, fmt.Errorf("lock slot: %w", err)
	}
	return slot, nil
}

type bookingQuerier interface {
	QueryRow(context.Context, string, ...any) pgx.Row
}

func bookingByID(ctx context.Context, db bookingQuerier, id string) (booking.Booking, bool, error) {
	var b booking.Booking
	if err := db.QueryRow(ctx, bookingSelectSQL()+`
WHERE b.id = $1`, id).Scan(bookingScanDest(&b)...); err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return booking.Booking{}, false, nil
		}
		return booking.Booking{}, false, fmt.Errorf("query booking by id: %w", err)
	}
	return b, true, nil
}

func bookingSelectSQL() string {
	return `
SELECT
    b.id::text,
    b.slot_id::text,
    b.client_id::text,
    b.equipment_type,
    b.status,
    b.created_at,
    b.cancelled_at,
    b.refund_amount,
    b.review_rating,
    b.review_text,
    s.id::text,
    s.start_at,
    s.menu,
    s.photo_urls,
    s.difficulty,
    s.capacity,
    s.booked_count,
    s.price,
    s.address,
    s.status,
    i.id::text,
    i.name,
    i.status,
    i.rating,
    i.specialization
FROM bookings b
JOIN slots s ON s.id = b.slot_id
JOIN instructors i ON i.id = s.instructor_id
`
}

func scanBooking(scanner interface{ Scan(...any) error }) (booking.Booking, error) {
	var b booking.Booking
	if err := scanner.Scan(bookingScanDest(&b)...); err != nil {
		return booking.Booking{}, fmt.Errorf("scan booking: %w", err)
	}
	return b, nil
}

func bookingScanDest(b *booking.Booking) []any {
	var slotPhotoUrls []string
	var slotInstructorSpecialization *string
	return []any{
		&b.ID,
		&b.SlotID,
		&b.ClientID,
		&b.EquipmentType,
		&b.Status,
		&b.CreatedAt,
		&b.CancelledAt,
		&b.RefundAmount,
		&b.ReviewRating,
		&b.ReviewText,
		&b.Slot.ID,
		&b.Slot.DateTime,
		&b.Slot.Menu,
		&slotPhotoUrls,
		&b.Slot.Difficulty,
		&b.Slot.Capacity,
		&b.Slot.BookedCount,
		&b.Slot.Price,
		&b.Slot.Address,
		&b.Slot.Status,
		&b.Slot.InstructorID,
		&b.Slot.InstructorName,
		&b.Slot.InstructorStatus,
		&b.Slot.InstructorRating,
		&slotInstructorSpecialization,
	}
}

func isUniqueViolation(err error) bool {
	var pgErr *pgconn.PgError
	return errors.As(err, &pgErr) && pgErr.Code == "23505"
}

package postgres

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Slot struct {
	ID                       string
	Menu                     string
	Difficulty               string
	PhotoUrls                []string
	InstructorID             string
	InstructorName           string
	InstructorStatus         string
	InstructorRating         float64
	InstructorSpecialization *string
	DateTime                 time.Time
	Capacity                 int
	BookedCount              int
	Price                    float64
	Address                  string
	Status                   string
}

type SlotFilters struct {
	DateFrom *time.Time
	DateTo   *time.Time
	Limit    int
	Offset   int
}

type SlotList struct {
	Items []Slot
	Total int
}

type SlotRepository struct {
	db *pgxpool.Pool
}

func NewSlotRepository(db *pgxpool.Pool) *SlotRepository {
	return &SlotRepository{db: db}
}

func (r *SlotRepository) List(ctx context.Context, filters SlotFilters) (SlotList, error) {
	where, args := slotWhere(filters)
	limit := filters.Limit
	if limit == 0 {
		limit = 20
	}
	offset := filters.Offset

	var total int
	if err := r.db.QueryRow(ctx, `SELECT count(*) FROM slots s JOIN instructors i ON i.id = s.instructor_id`+where, args...).Scan(&total); err != nil {
		return SlotList{}, fmt.Errorf("count slots: %w", err)
	}

	queryArgs := append(args, limit, offset)
	rows, err := r.db.Query(ctx, slotSelectSQL()+`
FROM slots s
JOIN instructors i ON i.id = s.instructor_id
`+where+`
ORDER BY s.start_at ASC
LIMIT $`+fmt.Sprint(len(args)+1)+` OFFSET $`+fmt.Sprint(len(args)+2), queryArgs...)
	if err != nil {
		return SlotList{}, fmt.Errorf("query slots: %w", err)
	}
	defer rows.Close()

	slots := make([]Slot, 0)
	for rows.Next() {
		slot, err := scanSlot(rows)
		if err != nil {
			return SlotList{}, err
		}
		slots = append(slots, slot)
	}
	if err := rows.Err(); err != nil {
		return SlotList{}, fmt.Errorf("iterate slots: %w", err)
	}

	return SlotList{Items: slots, Total: total}, nil
}

func (r *SlotRepository) GetByID(ctx context.Context, id string) (Slot, bool, error) {
	var slot Slot
	var instructorSpecialization *string
	var photoUrls []string
	err := r.db.QueryRow(ctx, slotSelectSQL()+`
FROM slots s
JOIN instructors i ON i.id = s.instructor_id
WHERE s.id = $1`, id).Scan(
		&slot.ID,
		&slot.DateTime,
		&slot.Menu,
		&photoUrls,
		&slot.Difficulty,
		&slot.InstructorID,
		&slot.InstructorName,
		&slot.InstructorStatus,
		&slot.InstructorRating,
		&instructorSpecialization,
		&slot.Capacity,
		&slot.BookedCount,
		&slot.Price,
		&slot.Address,
		&slot.Status,
	)
	if errors.Is(err, pgx.ErrNoRows) {
		return Slot{}, false, nil
	}
	if err != nil {
		return Slot{}, false, fmt.Errorf("get slot: %w", err)
	}
	slot.PhotoUrls = photoUrls
	slot.InstructorSpecialization = instructorSpecialization
	return slot, true, nil
}

func (r *SlotRepository) Create(ctx context.Context, slot Slot) error {
	_, err := r.db.Exec(ctx, `
INSERT INTO slots (id, menu, difficulty, photo_urls, instructor_id, start_at, capacity, price, address, status, booked_count)
VALUES ($1::uuid, $2, $3, $4, $5::uuid, $6, $7, $8, $9, $10, $11)`,
		slot.ID, slot.Menu, slot.Difficulty, slot.PhotoUrls, slot.InstructorID,
		slot.DateTime, slot.Capacity, slot.Price, slot.Address, slot.Status, slot.BookedCount,
	)
	if err != nil {
		return fmt.Errorf("create slot: %w", err)
	}
	return nil
}

func (r *SlotRepository) Update(ctx context.Context, slot Slot) error {
	_, err := r.db.Exec(ctx, `
UPDATE slots
SET menu = $2, difficulty = $3, photo_urls = $4, instructor_id = $5::uuid,
    start_at = $6, capacity = $7, price = $8, address = $9, status = $10
WHERE id = $1::uuid`, slot.ID, slot.Menu, slot.Difficulty, slot.PhotoUrls, slot.InstructorID,
		slot.DateTime, slot.Capacity, slot.Price, slot.Address, slot.Status)
	if err != nil {
		return fmt.Errorf("update slot: %w", err)
	}
	return nil
}

func (r *SlotRepository) Delete(ctx context.Context, id string) error {
	_, err := r.db.Exec(ctx, `DELETE FROM slots WHERE id = $1::uuid`, id)
	if err != nil {
		return fmt.Errorf("delete slot: %w", err)
	}
	return nil
}

func slotSelectSQL() string {
	return `
SELECT
    s.id::text,
    s.start_at,
    s.menu,
    s.photo_urls,
    s.difficulty,
    i.id::text,
    i.name,
    i.status,
    i.rating,
    i.specialization,
    s.capacity,
    s.booked_count,
    s.price,
    s.address,
    s.status`
}

func scanSlot(scanner interface{ Scan(...any) error }) (Slot, error) {
	var slot Slot
	var instructorSpecialization *string
	var photoUrls []string
	if err := scanner.Scan(
		&slot.ID,
		&slot.DateTime,
		&slot.Menu,
		&photoUrls,
		&slot.Difficulty,
		&slot.InstructorID,
		&slot.InstructorName,
		&slot.InstructorStatus,
		&slot.InstructorRating,
		&instructorSpecialization,
		&slot.Capacity,
		&slot.BookedCount,
		&slot.Price,
		&slot.Address,
		&slot.Status,
	); err != nil {
		return Slot{}, fmt.Errorf("scan slot: %w", err)
	}
	slot.PhotoUrls = photoUrls
	slot.InstructorSpecialization = instructorSpecialization
	return slot, nil
}

func slotWhere(filters SlotFilters) (string, []any) {
	conditions := make([]string, 0)
	args := make([]any, 0)
	add := func(condition string, arg any) {
		args = append(args, arg)
		conditions = append(conditions, fmt.Sprintf(condition, len(args)))
	}
	if filters.DateFrom != nil {
		add("s.start_at >= $%d", *filters.DateFrom)
	}
	if filters.DateTo != nil {
		add("s.start_at <= $%d", *filters.DateTo)
	}
	if len(conditions) == 0 {
		return "", args
	}
	return " WHERE " + strings.Join(conditions, " AND "), args
}

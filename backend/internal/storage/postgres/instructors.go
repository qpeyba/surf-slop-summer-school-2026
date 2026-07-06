package postgres

import (
	"context"
	"errors"
	"fmt"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Instructor struct {
	ID             string
	Name           string
	Status         string
	Rating         float64
	Specialization *string
}

type InstructorRepository struct {
	db *pgxpool.Pool
}

func NewInstructorRepository(db *pgxpool.Pool) *InstructorRepository {
	return &InstructorRepository{db: db}
}

func (r *InstructorRepository) ListInstructors(ctx context.Context) ([]Instructor, error) {
	rows, err := r.db.Query(ctx, `
SELECT id::text, name, status, rating, specialization
FROM instructors
ORDER BY name`)
	if err != nil {
		return nil, fmt.Errorf("list instructors: %w", err)
	}
	defer rows.Close()

	instructors := make([]Instructor, 0)
	for rows.Next() {
		var inst Instructor
		if err := rows.Scan(&inst.ID, &inst.Name, &inst.Status, &inst.Rating, &inst.Specialization); err != nil {
			return nil, fmt.Errorf("scan instructor: %w", err)
		}
		instructors = append(instructors, inst)
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate instructors: %w", err)
	}
	return instructors, nil
}

func (r *InstructorRepository) GetByID(ctx context.Context, id string) (Instructor, bool, error) {
	var instructor Instructor
	err := r.db.QueryRow(ctx, `
SELECT id::text, name, status, rating, specialization
FROM instructors
WHERE id = $1`, id).Scan(
		&instructor.ID,
		&instructor.Name,
		&instructor.Status,
		&instructor.Rating,
		&instructor.Specialization,
	)
	if errors.Is(err, pgx.ErrNoRows) {
		return Instructor{}, false, nil
	}
	if err != nil {
		return Instructor{}, false, fmt.Errorf("get instructor: %w", err)
	}
	return instructor, true, nil
}

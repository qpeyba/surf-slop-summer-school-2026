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
